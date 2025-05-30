package com.manwe.dsl.dedicatedServer.proxy.back.listeners;

import com.manwe.dsl.connectionRouting.RegionRouter;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.ProxyBoundContainerPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.ProxyBoundPlayerTransferACKPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.ProxyBoundPlayerTransferPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.WorkerBoundPlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.WorkerBoundPlayerTransferPacket;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener de los paquetes de los workers
 * Maneja los paquetes que le llegan desde los workers
 */
public class ProxyListenerImpl implements ProxyListener {

    //Players transferring waiting for the handlePlayerTransferACK from the receiving worker
    private final Set<UUID> pendingTransfers = ConcurrentHashMap.newKeySet();

    private final ChannelPipeline pipeline; //Proxy <-> Worker pipeline
    private final RegionRouter router;

    public ProxyListenerImpl(ChannelPipeline pipeline, RegionRouter router) {
        this.pipeline = pipeline;
        this.router = router;
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
        router.returnToClient(packet.getPlayerId(),packet.getPayload());
    }

    @Override
    public void handlePlayerTransfer(ProxyBoundPlayerTransferPacket packet) {
        System.out.println("RECEIVED PLAYER TRANSFER");
        System.out.println("Disconnecting from old worker");
        System.out.println("Send Transfer Request to worker "+packet.getWorkerId());
        //UUID playerId = packet.getGameProfile().getId();
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

        router.route(packet.getPlayerId()).send(new WorkerBoundPlayerDisconnectPacket(packet.getPlayerId())); //This points to the old worker
        router.transferClientToWorker(packet.getPlayerId(),packet.getWorkerId());//Change to the new worker
    }

}
