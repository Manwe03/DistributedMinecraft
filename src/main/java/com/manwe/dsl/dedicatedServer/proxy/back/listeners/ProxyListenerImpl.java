package com.manwe.dsl.dedicatedServer.proxy.back.listeners;

import com.manwe.dsl.connectionRouting.RegionRouter;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.WorkerProxyPacket;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;

/**
 * Listener de los paquetes de los workers
 * Maneja los paquetes que le llegan desde los workers
 */
public class ProxyListenerImpl implements ProxyListener {

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
    public void handleWorkerProxyPacket(WorkerProxyPacket packet) {
        //System.out.println("Proxy - Handle - Paquete recibido en el Proxy");
        //Obtener la connexion con el cliente
        //Mandar por esa conexión el paquete
        router.returnToClient(packet.getPlayerId(),packet.getPayload());
    }

}
