package com.manwe.dsl.dedicatedServer.worker.listeners;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.WorkerBoundPlayerInitPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.WorkerBoundPlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.worker.ProxyPlayerConnection;
import com.manwe.dsl.dedicatedServer.worker.packets.ProxyWorkerPacket;
import com.manwe.dsl.mixin.accessors.ConnectionAccessor;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ServerboundPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerListenerImpl implements WorkerListener {

    private final MinecraftServer server;
    private final ChannelPipeline pipeline;
    private final Map<UUID, Connection> playerConnections = new ConcurrentHashMap<>();

    public WorkerListenerImpl(MinecraftServer server, ChannelPipeline pipeline) {
        this.server = server;
        this.pipeline = pipeline;
    }

    @Override
    public void onDisconnect(DisconnectionDetails pDetails) {
        System.out.println("Worker<->Proxy tunnel Disconnected");
    }

    @Override
    public boolean isAcceptingMessages() {
        return pipeline.channel().isOpen();
    }

    ////////////////////////////////////////////////////
    /// Internal message Listener Server Side        ///
    /// These are the packets sent by the proxy to   ///
    /// the workers as internal communication        ///
    ////////////////////////////////////////////////////

    @Override
    public void handleProxyWorkerPacket(ProxyWorkerPacket packet) {
        //Ejecuta el paquete interno con el listener asociado al UUID
        Connection connection = playerConnections.get(packet.getPlayerId());
        if(connection == null) {
            DistributedServerLevels.LOGGER.warn("ServerPlayer with UUID "+packet.getPlayerId()+" does not have a listener. (skipped player login?)");
        } else if(connection.getPacketListener() instanceof WorkerGamePacketListenerImpl serverGamePacketListener) {
            server.execute(()->{ //Run on the minecraft main thread instead of the I/O thread
                ((Packet<ServerGamePacketListener>) packet.getPayload()).handle(serverGamePacketListener);
            });
        } else {
            Component component = Component.translatable("disconnect.genericReason", "Internal Exception"); //TODO código de prueba
            connection.send(new ClientboundDisconnectPacket(component));
        }
    }

    @Override
    public void handlePlayerLogin(WorkerBoundPlayerInitPacket packet) {
        System.out.println("capturado handlePlayerLogin en el loginListener");

        //TODO hay que ver esto de las dimensiones
        ServerPlayer player = packet.rebuildServerPlayer(server);

        //Create new connection and listener
        Connection connection = new ProxyPlayerConnection(PacketFlow.CLIENTBOUND, player.getUUID(),server.registryAccess());
        WorkerGamePacketListenerImpl playerListener = new WorkerGamePacketListenerImpl(server,connection,player,packet.rebuildCookie());
        try {
            ((ConnectionAccessor) connection).setChannel(pipeline.channel());
            ((ConnectionAccessor) connection).setPacketListener(playerListener);
            this.playerConnections.put(player.getUUID(),connection); //Hold in map
        }catch (Exception e){
            DistributedServerLevels.LOGGER.error("Error setting channelActive");
        }

        server.getConnection().getConnections().add(connection); //Añadir las fake connections a la lista de conexiones del servidor para que hagan tick()

        System.out.println("player"+ player.getDisplayName().getString());
        server.getPlayerList().placeNewPlayer(connection,player,packet.rebuildCookie());
        //TODO place player in world
    }

    @Override
    public void handlePlayerDisconnect(WorkerBoundPlayerDisconnectPacket packet) {
        server.execute(()->{ //Run on the minecraft main thread instead of the I/O thread
            //Disconnect (gamelogic) call to this player gameListener
            PacketListener gameListener = getDedicatedPlayerListener(packet.getPlayerID());
            gameListener.onDisconnect(packet.getDefaultDisconnectionDetails());
            //Remove from map
            Connection removed = playerConnections.remove(packet.getPlayerID());
            //Remove from ServerConnectionListener to stop tick() and avoid duplicates
            server.getConnection().getConnections().remove(removed);
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
