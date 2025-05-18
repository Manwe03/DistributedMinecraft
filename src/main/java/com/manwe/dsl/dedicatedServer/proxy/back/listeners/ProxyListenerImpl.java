package com.manwe.dsl.dedicatedServer.proxy.back.listeners;

import com.manwe.dsl.dedicatedServer.proxy.back.packets.WorkerProxyPacket;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;

/**
 * Listener de los paquetes de los workers
 * Maneja los paquetes que le llegan desde los workers
 */
public class ProxyListenerImpl implements ProxyListener {

    private final Connection connection;

    public ProxyListenerImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onDisconnect(DisconnectionDetails pDetails) {

    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    /**
     * handle packets sent by workers
     * @param packet
     */
    @Override
    public void handleWorkerProxyPacket(WorkerProxyPacket packet) {
        System.out.println("Worker -> Proxy - Paquete recibido en el Proxy");
        packet.handle(this);
    }
}
