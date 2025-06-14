package com.manwe.dsl.dedicatedServer.proxy.back.listeners;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.connectionRouting.RegionRouter;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.*;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerInformationPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerLoginPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerMovePacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.login.ProxyBoundLevelInformationPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.login.ProxyBoundPlayerInitACKPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.transfer.ProxyBoundPlayerTransferACKPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.transfer.ProxyBoundPlayerTransferPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.chunkloading.WorkerBoundFakePlayerInformationPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.chunkloading.WorkerBoundFakePlayerLoginPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.chunkloading.WorkerBoundFakePlayerMovePacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.login.WorkerBoundPlayerLoginACKPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerEndTransferPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerTransferPacket;
import io.netty.channel.ChannelPipeline;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener de los paquetes de los workers
 * Maneja los paquetes que le llegan desde los workers
 */
public class ProxyListenerImpl implements ProxyListener {

    //Players transferring waiting for the handlePlayerTransferACK from the receiving worker
    private final Set<UUID> pendingTransfers = ConcurrentHashMap.newKeySet();
    private final Map<UUID,Runnable> pendingLogin = new HashMap<>();

    private final ChannelPipeline pipeline; //Proxy <-> Worker pipeline
    private final RegionRouter router;
    private final Path playerDir;

    public ProxyListenerImpl(ChannelPipeline pipeline, RegionRouter router, Path playerDir) {
        this.pipeline = pipeline;
        this.router = router;
        this.playerDir = playerDir;
    }

    @Override
    public void onDisconnect(DisconnectionDetails pDetails) {
        System.out.println("PROXY onDisconnect");
    }

    @Override
    public boolean isAcceptingMessages() {
        return pipeline.channel().isOpen();
    }

    @Override
    public ConnectionProtocol protocol() {
        return ProxyListener.super.protocol();
    }

    /**
     * Delays this login code until the proxy receives the worker confirmation that it has created the player
     * @param uuid Owner
     * @param runnable Code
     */
    @Override
    public void addPendingLogin(UUID uuid, Runnable runnable){
        pendingLogin.put(uuid,runnable);
    }

    ////////////////////////////////////////////////////
    /// Internal message Listener Client Side        ///
    /// These are the packets sent by the workers    ///
    /// to the proxy as internal communication       ///
    ////////////////////////////////////////////////////

    /**
     * handle packets sent by workers
     * @param packet
     */
    @Override
    public void handleWorkerProxyPacket(ProxyBoundContainerPacket packet) {
        //System.out.println("Proxy - Handle - Paquete recibido en el Proxy");
        //Obtener la connexion con el cliente
        //Mandar por esa conexión el paquete
        if(packet.getPayload().type() == CommonPacketTypes.CLIENTBOUND_DISCONNECT){
            System.out.println("Client Bound Disconnect");
        }
        router.returnToClient(packet.getPlayerId(),packet.getPayload());
    }

    @Override
    public void handlePlayerTransfer(ProxyBoundPlayerTransferPacket packet) {
        System.out.println("RECEIVED PLAYER TRANSFER");
        if (!pendingTransfers.add(packet.getGameProfile().getId())){ //Block duplicated transfers
            System.out.println("Can not handle transfer. Waiting for worker to respond with ACK");
            return;
        }
        router.route(packet.getWorkerId()).send(new WorkerBoundPlayerTransferPacket(packet)); //Send to worker
    }

    @Override
    public void handlePlayerTransferACK(ProxyBoundPlayerTransferACKPacket packet) {
        System.out.println("Transfer ACK, moving player to worker "+packet.getWorkerId());
        pendingTransfers.remove(packet.getPlayerId());

        router.route(packet.getPlayerId()).send(new WorkerBoundPlayerEndTransferPacket(packet.getPlayerId())); //This points to the old worker
        router.transferClientToWorker(packet.getPlayerId(),packet.getWorkerId());//Change to the new worker
    }

    @Override
    public void handleSavePlayerState(ProxyBoundSavePlayerStatePacket packet) {
        Vec3 position = packet.getPosition();
        UUID playerId = packet.getPlayerId();

        CompoundTag tag = new CompoundTag();

        // dimensión
        tag.putString("Dimension", packet.getDimension());

        // posición (ListTag [X, Y, Z])
        ListTag posTag = new ListTag();
        posTag.add(DoubleTag.valueOf(position.x));
        posTag.add(DoubleTag.valueOf(position.y));
        posTag.add(DoubleTag.valueOf(position.z));
        tag.put("Pos", posTag);

        // campo extra (no vanilla) para el sistema distribuido
        tag.putInt("WorkerId", packet.getWorkerId());

        try {
            Files.createDirectories(this.playerDir);
            Path file = playerDir.resolve(playerId + ".dat");

            NbtIo.writeCompressed(tag, file);

        } catch (IOException ex) {
            DistributedServerLevels.LOGGER.error("Couldn't save player-data for {}", playerId, ex);
        }

        System.out.println("SAVED PLAYER STATE IN PROXY AFTER DISCONNECTION");
    }

    @Override
    public void handlePlayerInitACK(ProxyBoundPlayerInitACKPacket packet) {
        System.out.println("Received init ACK, sending ClientboundLoginPacket");
        pendingLogin.get(packet.getPlayerId()).run();

        router.route(packet.getPlayerId()).send(new WorkerBoundPlayerLoginACKPacket(packet.getPlayerId())); //Send ack proxy has sent the login packet
        System.out.println("proxy has sent the login packet");
    }

    @Override
    public void handleFakePlayerLogin(ProxyBoundFakePlayerLoginPacket packet) {
        packet.getWorkers().stream().forEach(id -> {    //Login all fake players is other workers
            router.route(id).send(new WorkerBoundFakePlayerLoginPacket(packet));
        });
    }

    @Override
    public void handleFakePlayerMove(ProxyBoundFakePlayerMovePacket packet) {
        packet.getWorkers().stream().forEach(id -> {    //Move all fake players is other workers
            router.route(id).send(new WorkerBoundFakePlayerMovePacket(packet));
        });
    }

    @Override
    public void handleFakePlayerDisconnect(ProxyBoundFakePlayerDisconnectPacket packet) {
        packet.getWorkers().stream().forEach(id -> {    //Disconnect all fake players in other workers
            router.route(id).send(new WorkerBoundPlayerDisconnectPacket(packet.getPlayerId(),true));
        });
    }

    @Override
    public void handleFakePlayerInformation(ProxyBoundFakePlayerInformationPacket packet) {
        packet.getWorkers().stream().forEach(id -> {
            router.route(id).send(new WorkerBoundFakePlayerInformationPacket(packet));
        });
    }

    @Override
    public void handleLevelInformation(ProxyBoundLevelInformationPacket packet) {
        router.setDefaultSpawn(packet.getDefaultSpawnPos());
    }

}
