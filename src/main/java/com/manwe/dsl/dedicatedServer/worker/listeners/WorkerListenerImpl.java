package com.manwe.dsl.dedicatedServer.worker.listeners;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.config.DSLServerConfigs;
import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.login.ProxyBoundLevelInformationPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.transfer.ProxyBoundPlayerTransferACKPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.ProxyBoundSavePlayerStatePacket;
import com.manwe.dsl.dedicatedServer.worker.FakePlayerConnection;
import com.manwe.dsl.dedicatedServer.worker.LocalPlayerList;
import com.manwe.dsl.dedicatedServer.worker.chunk.ChunkLoadingFakePlayer;
import com.manwe.dsl.dedicatedServer.worker.packets.*;
import com.manwe.dsl.dedicatedServer.worker.ProxyPlayerConnection;
import com.manwe.dsl.dedicatedServer.worker.packets.chunkloading.WorkerBoundFakePlayerInformationPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.chunkloading.WorkerBoundFakePlayerLoginPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.chunkloading.WorkerBoundFakePlayerMovePacket;
import com.manwe.dsl.dedicatedServer.worker.packets.login.WorkerBoundPlayerLoginACKPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.login.WorkerBoundPlayerLoginPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.login.WorkerBoundRequestLevelInformationPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerEndTransferPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerTransferPacket;
import com.manwe.dsl.mixin.accessors.ConnectionAccessor;
import com.manwe.dsl.mixin.accessors.ServerPlayerAccessor;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerListenerImpl implements WorkerListener {

    private final MinecraftServer server;
    private final ChannelPipeline sharedPipeline;
    private final Map<UUID, Connection> playerConnections = new ConcurrentHashMap<>();
    private final Set<UUID> transferring = new HashSet<>(); //All player that are currently being transferred to another worker

    private final Map<UUID, List<WorkerBoundContainerPacket>> earlyPackets = new ConcurrentHashMap<>();

    int workerId = DSLServerConfigs.WORKER_ID.get();

    public WorkerListenerImpl(MinecraftServer server, ChannelPipeline sharedPipeline) {
        this.server = server;
        this.sharedPipeline = sharedPipeline;
    }

    @Override
    public void onDisconnect(DisconnectionDetails pDetails) {
        System.out.println("Worker<->Proxy tunnel Disconnected");
    }

    @Override
    public boolean isAcceptingMessages() {
        return sharedPipeline.channel().isOpen();
    }

    ////////////////////////////////////////////////////
    /// Internal message Listener Server Side        ///
    /// These are the packets sent by the proxy to   ///
    /// the workers as internal communication        ///
    ////////////////////////////////////////////////////

    @Override
    public void handleProxyWorkerPacket(WorkerBoundContainerPacket packet) {

        UUID uuid = packet.getPlayerId();

        if (!playerConnections.containsKey(uuid)) {
            earlyPackets.computeIfAbsent(uuid, k -> new LinkedList<>()).add(packet);
            System.out.println("Packet Received before login finished - Stored");
            return;
        }

        //Ejecuta el paquete interno con el listener asociado al UUID
        Connection connection = playerConnections.get(uuid);
        if(connection == null) {
            DistributedServerLevels.LOGGER.warn("ServerPlayer with UUID ["+uuid+"] does not have a listener");
        } else if (transferring.contains(uuid)) {
            DistributedServerLevels.LOGGER.info("This player is being transferred to another worker");
        } else if(connection.getPacketListener() instanceof WorkerGamePacketListenerImpl serverGamePacketListener) {
            server.execute(()->{ //Run on the minecraft main thread instead of the I/O thread
                ((Packet<ServerGamePacketListener>) packet.getPayload()).handle(serverGamePacketListener);
            });
        } else if (connection.getPacketListener() instanceof WorkerFakePlayerListenerImpl fakeGamePacketListener) {
            server.execute(()->{ //Run on the minecraft main thread instead of the I/O thread
                System.out.println("WorkerFakePlayerListenerImpl handling: "+packet.getPayload().type());
                ((Packet<ServerGamePacketListener>) packet.getPayload()).handle(fakeGamePacketListener);
            });
        } else {
            System.out.println("ERROR inesperado");
            Component component = Component.translatable("disconnect.genericReason", "Internal Exception"); //TODO código de prueba
            connection.send(new ClientboundDisconnectPacket(component));
        }
    }

    @Override
    public void handlePlayerLogin(WorkerBoundPlayerLoginPacket packet) {
        ServerPlayer player = packet.rebuildServerPlayer(server);
        CommonListenerCookie cookie = packet.rebuildCookie();
        DistributedServerLevels.LOGGER.info("New player ["+player.getDisplayName().getString()+"] logged into worker ["+ DSLServerConfigs.WORKER_ID.get()+"]");

        Connection proxyConnection = generateConnection(player, cookie, new BitSet());

        server.execute(()-> {
            if(!(server.getPlayerList() instanceof LocalPlayerList localPlayerList)) throw new RuntimeException("PlayerList is not an instance of LocalPlayerList");
            registerPlayerAndConnection(player, proxyConnection);
            localPlayerList.placeNewPlayer(proxyConnection, player, cookie);
            if(proxyConnection.getPacketListener() instanceof WorkerGamePacketListenerImpl gamePacketListener) gamePacketListener.updateFakePlayers();
            DistributedServerLevels.LOGGER.info("Player [" + player.getDisplayName().getString() + "] placed in world at X:" + player.getX() + " Z:" + player.getZ());
        });
    }

    /**
     * Handles player transfer request from proxy
     * @param packet
     */
    @Override
    public void handlePlayerTransfer(WorkerBoundPlayerTransferPacket packet) {
        ServerPlayer clonePlayer = packet.rebuildServerPlayer(server);
        CommonListenerCookie cookie = packet.rebuildCookie();

        clonePlayer.load(packet.getPlayerNbt());

        server.execute(()-> {
            if (!(server.getPlayerList() instanceof LocalPlayerList localPlayerList)) throw new RuntimeException("Worker must have a localPlayerList");

            ServerPlayer serverPlayer = localPlayerList.getPlayer(clonePlayer.getUUID());
            if(serverPlayer instanceof ChunkLoadingFakePlayer fakePlayer){ //Fake player was preloaded in this worker
                System.out.println("Disconnect fake player to replace it with cloned ServerPlayer");
                fakePlayer.connection.onDisconnect(packet.getDefaultDisconnectionDetails()); //Disconnect
            }

            Connection proxyConnection = generateConnection(clonePlayer, cookie, packet.getWorkers()); //Generate connection and listener with bitset

            registerPlayerAndConnection(clonePlayer, proxyConnection);

            localPlayerList.transferExistingPlayer(clonePlayer, packet.getPlayerNbt());

            this.send(new ProxyBoundPlayerTransferACKPacket(this.workerId, clonePlayer.getUUID())); //Send Setup to proxy router for the new worker redirection
            if(proxyConnection.getPacketListener() instanceof WorkerGamePacketListenerImpl gamePacketListener) gamePacketListener.updateFakePlayers();
            DistributedServerLevels.LOGGER.info("New player [" + clonePlayer.getDisplayName().getString() + "] transferred into worker [" + DSLServerConfigs.WORKER_ID.get() + "]");
        });
    }

    @Override
    public void handleFakePlayerLogin(WorkerBoundFakePlayerLoginPacket packet) {
        Optional<ServerLevel> serverLevel = Optional.ofNullable(server.getLevel(packet.levelResourcekey));
        ChunkLoadingFakePlayer player = new ChunkLoadingFakePlayer(server, packet);
        Connection proxyConnection = generateFakePlayerConnection(player, CommonListenerCookie.createInitial(packet.gameprofile,false));

        server.execute(()-> {
            if(!(server.getPlayerList() instanceof LocalPlayerList localPlayerList)) throw new RuntimeException("PlayerList is not an instance of LocalPlayerList");
            localPlayerList.placeNewChunkLoadingFakePlayer(serverLevel.orElse(server.overworld()), player);
            registerPlayerAndConnection(player, proxyConnection);
            DistributedServerLevels.LOGGER.info("Fake Player ["+player.getDisplayName().getString()+"] placed in world at "+player.position());
        });
    }

    private void registerPlayerAndConnection(ServerPlayer player, Connection proxyConnection) {
        System.out.println("Set Player Connection with UUID: "+player.getUUID());
        playerConnections.put(player.getUUID(), proxyConnection); //Set this connection as a player connection only after player is placed in world
        server.getConnection().getConnections().add(proxyConnection); //Add listener for ticking
    }

    @Override
    public void handlePlayerLoginACK(WorkerBoundPlayerLoginACKPacket packet) {
        System.out.println("RUNNING REST OF placeNewPlayer()");
        UUID uuid = packet.getPlayerId();
        if(!(playerConnections.get(uuid).getPacketListener() instanceof WorkerGamePacketListenerImpl serverGamePacketListener)) throw new RuntimeException("listener is not instance of WorkerGamePacketListenerImpl");
        server.execute(()-> {
            if(earlyPackets.containsKey(uuid)){
                earlyPackets.get(uuid).forEach(workerBoundContainerPacket -> { //Run pending packets before login completed
                    ((Packet<ServerGamePacketListener>) workerBoundContainerPacket.getPayload()).handle(serverGamePacketListener);
                });
            }
        });
    }

    /**
     * Creates the fake connection with the proxy
     * Creates the ServerGameListener for this player
     * @return
     */
    @NotNull
    private Connection generateConnection(ServerPlayer player, CommonListenerCookie cookie, BitSet preloadedWorkers) {
        //Create new connection and listener
        Connection connection = new ProxyPlayerConnection(PacketFlow.CLIENTBOUND, player.getUUID(),server.registryAccess());
        WorkerGamePacketListenerImpl playerListener = new WorkerGamePacketListenerImpl(server,connection, player, cookie,this, preloadedWorkers);
        ((ConnectionAccessor) connection).setChannel(sharedPipeline.channel());
        ((ConnectionAccessor) connection).setPacketListener(playerListener);
        return connection;
    }

    /**
     * Creates the fake connection with the proxy
     * Creates the ServerGameListener for this player
     * @return
     */
    @NotNull
    private Connection generateFakePlayerConnection(ServerPlayer player, CommonListenerCookie cookie) {
        //Create new connection and listener
        Connection connection = new FakePlayerConnection(PacketFlow.CLIENTBOUND, player.getUUID(),server.registryAccess());
        WorkerFakePlayerListenerImpl playerListener = new WorkerFakePlayerListenerImpl(server,connection, player, cookie,this);
        ((ConnectionAccessor) connection).setChannel(sharedPipeline.channel());
        ((ConnectionAccessor) connection).setPacketListener(playerListener);
        return connection;
    }

    /**
     * Handle disconnection of real and fake players
     */
    @Override
    public void handlePlayerDisconnect(WorkerBoundPlayerDisconnectPacket packet) {
        System.out.println("Player Disconnect Requested By Proxy Handling in worker");
        UUID playerId = packet.getPlayerID();
        boolean fakePlayer = packet.isFakePlayer();

        ServerPlayer disconnectedPlayer = server.getPlayerList().getPlayer(playerId);
        if(disconnectedPlayer == null) throw new RuntimeException("handlePlayerDisconnect: Player is null"); //TODO puede darse este problema

        if(!fakePlayer) this.send(new ProxyBoundSavePlayerStatePacket(playerId, workerId, disconnectedPlayer.position(), disconnectedPlayer.level().dimension().location().toString()));
        PacketListener gameListener = getDedicatedPlayerListener(playerId);

        server.execute(()->{ //Run on the minecraft main thread instead of the I/O thread
            //Disconnect (game logic) call to this player gameListener
            gameListener.onDisconnect(packet.getDefaultDisconnectionDetails());
            System.out.println("handlePlayerDisconnect remove connection "+playerId);
            Connection removed = playerConnections.remove(playerId);//Remove from map
            server.getConnection().getConnections().remove(removed);//Remove from ServerConnectionListener to stop tick() and avoid duplicates
        });
    }

    /**
     * Handle disconnection of transferred players
     */
    @Override
    public void handlePlayerEndTransfer(WorkerBoundPlayerEndTransferPacket packet) {
        UUID playerId = packet.getPlayerID();
        PacketListener gameListener = getDedicatedPlayerListener(playerId);
        if(!(gameListener instanceof WorkerGamePacketListenerImpl workerGameListener)) throw new RuntimeException("gameListener is not instance of WorkerGamePacketListenerImpl");

        server.execute(()->{
            //Disconnect (game logic) call to this player gameListener
            workerGameListener.silentDisconnect();
            System.out.println("handlePlayerEndTransfer remove connection "+playerId);
            Connection removed = playerConnections.remove(playerId); //Remove from map
            server.getConnection().getConnections().remove(removed); //Remove from ServerConnectionListener to stop tick() and avoid duplicates
            setFinishTransfering(playerId); //Remove form transferring
        });
        System.out.println("PlayerList: ");
        server.getPlayerList().getPlayers().forEach(System.out::println);
    }

    @Override
    public void handleFakePlayerMove(WorkerBoundFakePlayerMovePacket packet) {
        ServerPlayer serverPlayer = server.getPlayerList().getPlayer(packet.getPlayerId());
        if(serverPlayer instanceof ChunkLoadingFakePlayer fakePlayer){
            fakePlayer.absMoveTo(packet.getPos().x,packet.getPos().y,packet.getPos().z);
            System.out.println("Fake Player absMoveTo: " + fakePlayer.position());
        }
    }

    @Override
    public void handleFakePlayerInformation(WorkerBoundFakePlayerInformationPacket packet) {
        ServerPlayer player = server.getPlayerList().getPlayer(packet.getPlayerId());
        if(player instanceof ChunkLoadingFakePlayer fakePlayer){
            fakePlayer.setFakePlayerRequestedViewDistance(packet.getViewDistance());
        } else {
            DistributedServerLevels.LOGGER.error("Tried to set view distance to a fake player that does not exists");
        }
    }

    @Override
    public void handleLevelInformation(WorkerBoundRequestLevelInformationPacket packet) {
        this.send(new ProxyBoundLevelInformationPacket(server.overworld().getSharedSpawnPos()));
    }

    private PacketListener getDedicatedPlayerListener(UUID playerId){
        return this.playerConnections.get(playerId).getPacketListener();
    }

    public void setTransfering(UUID playerId){
        transferring.add(playerId);
    }

    public void setFinishTransfering(UUID playerId){
        transferring.remove(playerId);
    }

    /**
     * Send unwrapped ProxyBound packets
     */
    public void send(Packet<ProxyListener> packet){
        sharedPipeline.writeAndFlush(packet);
    }

    @Override
    public Connection getPlayerConnection(UUID playerId) {
        return this.playerConnections.get(playerId);
    }
}
