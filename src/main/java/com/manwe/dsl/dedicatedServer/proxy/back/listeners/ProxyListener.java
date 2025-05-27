package com.manwe.dsl.dedicatedServer.proxy.back.listeners;

import com.manwe.dsl.dedicatedServer.proxy.back.packets.WorkerProxyPacket;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public interface ProxyListener extends ClientboundPacketListener {
    @Override
    default ConnectionProtocol protocol() {
        return ConnectionProtocol.PLAY;
    }

    /**
     * handle packets sent by workers
     * @param packet
     */
    void handleWorkerProxyPacket(WorkerProxyPacket packet);
}
