package com.manwe.dsl.dedicatedServer.proxy.back.listeners;

import com.manwe.dsl.dedicatedServer.proxy.back.packets.ProxyBoundContainerPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.ProxyBoundPlayerTransferACKPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.ProxyBoundPlayerTransferPacket;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.ConnectionProtocol;

public interface ProxyListener extends ClientboundPacketListener {
    @Override
    default ConnectionProtocol protocol() {
        return ConnectionProtocol.PLAY;
    }

    /**
     * handle packets sent by workers
     * @param packet
     */
    void handleWorkerProxyPacket(ProxyBoundContainerPacket packet);

    void handlePlayerTransfer(ProxyBoundPlayerTransferPacket packet);

    void handlePlayerTransferACK(ProxyBoundPlayerTransferACKPacket packet);
}
