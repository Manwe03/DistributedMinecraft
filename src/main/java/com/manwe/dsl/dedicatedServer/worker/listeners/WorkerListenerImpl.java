package com.manwe.dsl.dedicatedServer.worker.listeners;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.config.DSLServerConfigs;
import com.manwe.dsl.connectionRouting.RegionRouter;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.ProxyBoundPlayerTransferACKPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.ProxyBoundSavePlayerStatePacket;
import com.manwe.dsl.dedicatedServer.worker.LocalPlayerList;
import com.manwe.dsl.dedicatedServer.worker.packets.*;
import com.manwe.dsl.dedicatedServer.worker.ProxyPlayerConnection;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.ProxyBoundPlayerTransferPacket;
import com.manwe.dsl.mixin.accessors.ConnectionAccessor;
import com.manwe.dsl.mixin.accessors.ServerLevelAccessor;
import io.netty.channel.ChannelPipeline;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerListenerImpl implements WorkerListener {

    private final MinecraftServer server;
    private final ChannelPipeline sharedPipeline;
    private final Map<UUID, Connection> playerConnections = new ConcurrentHashMap<>();
    private final Set<UUID> transferring = new HashSet<>(); //All player that are currently being transferred to another worker

    private final Map<UUID, Runnable> pendingLogin = new HashMap<>();

    private final Map<UUID, List<WorkerBoundContainerPacket>> earlyPackets = new ConcurrentHashMap<>();

    int workerId = DSLServerConfigs.WORKER_ID.get();
    int workerSize = DSLServerConfigs.WORKER_SIZE.get();
    int regionSize = DSLServerConfigs.REGION_SIZE.get();

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
                handleOutsideWorkerBounds(packet,server); //TODO ver si puede darse el caso de que entren varios paquetes de movimiento antes de que se marque como transferring
            });
        } else {
            System.out.println("ERROR inesperado");
            Component component = Component.translatable("disconnect.genericReason", "Internal Exception"); //TODO código de prueba
            connection.send(new ClientboundDisconnectPacket(component));
        }
    }

    /**
     * Handle send transfer request to proxy and removes this connection
     * @param packet
     * @return transferred
     */
    private void handleOutsideWorkerBounds(WorkerBoundContainerPacket packet, MinecraftServer server) {
        if(packet.getPayload() instanceof ServerboundMovePlayerPacket){
            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(packet.getPlayerId());
            if(serverPlayer == null) throw new RuntimeException("[handleOutsideWorkerBounds()] Player not found in worker");
            int id = RegionRouter.computeWorkerId(serverPlayer.getX(),serverPlayer.getZ(), workerSize, regionSize);
            if(id != workerId){
                System.out.println("Player outside worker position X:"+ serverPlayer.getX()+" Z:"+serverPlayer.getZ());
                //Transfer
                sharedPipeline.writeAndFlush(new ProxyBoundPlayerTransferPacket(serverPlayer, id));
                //connection.send(new ProxyBoundPlayerTransferPacket(server, packet.getPlayerId()));
                //Bloquear
                transferring.add(packet.getPlayerId());
                DistributedServerLevels.LOGGER.info("Transfer in progress block all incoming packets from ["+ packet.getPlayerId()+"] to this worker");
            }
        }
        //System.out.println("Inside bounds of "+workerId);
    }

    @Override
    public void handlePlayerLogin(WorkerBoundPlayerInitPacket packet) {
        System.out.println("Worker: handlePlayerLogin");

        ServerPlayer player = packet.rebuildServerPlayer(server);
        CommonListenerCookie cookie = packet.rebuildCookie();
        DistributedServerLevels.LOGGER.info("New player ["+player.getDisplayName().getString()+"] logged into worker ["+ DSLServerConfigs.WORKER_ID.get()+"]");

        Connection proxyConnection = generateConnection(player, cookie);

        server.execute(()-> {
            if(!(server.getPlayerList() instanceof LocalPlayerList localPlayerList)) throw new RuntimeException("PlayerList is not an instance of LocalPlayerList");

            localPlayerList.placeNewPlayer(proxyConnection, player, cookie, sharedPipeline);
            registerPlayerAndConnection(player, proxyConnection);

            System.out.println("Player ["+player.getDisplayName().getString()+"] placed in world");
        });
    }

    /**
     * Handles player transfer request from proxy
     * @param packet
     */
    @Override
    public void handlePlayerTransfer(WorkerBoundPlayerTransferPacket packet) {
        System.out.println("Worker: handlePlayerTransfer");

        ServerPlayer clonePlayer = packet.rebuildServerPlayer(server);
        CommonListenerCookie cookie = packet.rebuildCookie();
        DistributedServerLevels.LOGGER.info("New player [" + clonePlayer.getDisplayName().getString() + "] transferred into worker [" + DSLServerConfigs.WORKER_ID.get() + "]");

        clonePlayer.load(packet.getPlayerNbt());

        Connection proxyConnection = generateConnection(clonePlayer, cookie);

        server.execute(()-> {
            if (!(server.getPlayerList() instanceof LocalPlayerList localPlayerList)) throw new RuntimeException("Worker must have a localPlayerList");

            localPlayerList.transferExistingPlayer(clonePlayer, packet.getPlayerNbt());
            DistributedServerLevels.LOGGER.info("Player [" + clonePlayer.getDisplayName().getString() + "] placed in world at X:" + clonePlayer.getX() + " Z:" + clonePlayer.getZ());

            sharedPipeline.writeAndFlush(new ProxyBoundPlayerTransferACKPacket(this.workerId, clonePlayer.getUUID())); //Send Setup to proxy router for the new worker redirection
            registerPlayerAndConnection(clonePlayer, proxyConnection);
        });
    }

    private void registerPlayerAndConnection(ServerPlayer player, Connection proxyConnection) {
        playerConnections.put(player.getUUID(), proxyConnection); //Set this connection as a player connection only after player is placed in world
        server.getConnection().getConnections().add(proxyConnection); //Add listener for ticking
    }

    @Override
    public void handlePlayerLoginACK(WorkerBoundPlayerInitACKPacket packet) {
        System.out.println("RUNNING REST OF placeNewPlayer()");
        UUID uuid = packet.getPlayerId();
        if(!(playerConnections.get(uuid).getPacketListener() instanceof WorkerGamePacketListenerImpl serverGamePacketListener)) throw new RuntimeException("listener is not instance of WorkerGamePacketListenerImpl");
        server.execute(()-> {
            pendingLogin.get(uuid).run(); //Run pending login
            if(earlyPackets.containsKey(uuid)){
                System.out.println("Running stored packets");
                earlyPackets.get(uuid).forEach(workerBoundContainerPacket -> { //Run pending packets before login completed
                    ((Packet<ServerGamePacketListener>) workerBoundContainerPacket.getPayload()).handle(serverGamePacketListener);
                });
            } else {
                System.out.println("No stored packets");
            }
        });
    }

    /**
     * Creates the fake connection with the proxy
     * Creates the ServerGameListener for this player
     * @return
     */
    @NotNull
    private Connection generateConnection(ServerPlayer player, CommonListenerCookie cookie) {
        //Create new connection and listener
        Connection connection = new ProxyPlayerConnection(PacketFlow.CLIENTBOUND, player.getUUID(),server.registryAccess());
        WorkerGamePacketListenerImpl playerListener = new WorkerGamePacketListenerImpl(server,connection, player, cookie);
        try {
            ((ConnectionAccessor) connection).setChannel(sharedPipeline.channel());
            ((ConnectionAccessor) connection).setPacketListener(playerListener);
        }catch (Exception e){
            DistributedServerLevels.LOGGER.error("Error setting channelActive");
        }

        player.connection = playerListener; //Set this players listener
        //TODO hay que estudiar esto
        //server.getConnection().getConnections().add(connection); //Añadir las fake connections a la lista de conexiones del servidor para que hagan tick()
        return connection;
    }

    //Proxy detected a player disconnection
    @Override
    public void handlePlayerDisconnect(WorkerBoundPlayerDisconnectPacket packet) {
        System.out.println("handlePlayerDisconnect");
        UUID playerId = packet.getPlayerID();
        server.execute(()->{ //Run on the minecraft main thread instead of the I/O thread
            ServerPlayer disconnectedPlayer = server.getPlayerList().getPlayer(playerId);
            if(disconnectedPlayer == null) throw new RuntimeException("handlePlayerDisconnect: Player is null");
            //Send last player position
            sharedPipeline.writeAndFlush(new ProxyBoundSavePlayerStatePacket(playerId,workerId,disconnectedPlayer.position(),disconnectedPlayer.level().dimension().location().toString()));

            //Disconnect (gamelogic) call to this player gameListener
            PacketListener gameListener = getDedicatedPlayerListener(playerId);
            gameListener.onDisconnect(packet.getDefaultDisconnectionDetails()); //TODO hay que ver si todo este método hay que ejecutarlo o solo partes de el
            //Remove from map
            Connection removed = playerConnections.remove(playerId);
            //Remove from ServerConnectionListener to stop tick() and avoid duplicates
            server.getConnection().getConnections().remove(removed);
            //Remove form transferring
            transferring.remove(playerId);
        });
    }

    private PacketListener getDedicatedPlayerListener(UUID playerId){
        return this.playerConnections.get(playerId).getPacketListener();
    }

    @Override
    public Connection getPlayerConnection(UUID playerId) {
        return this.playerConnections.get(playerId);
    }
}
