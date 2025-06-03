package com.manwe.dsl.dedicatedServer.proxy.back.listeners;

import com.manwe.dsl.dedicatedServer.proxy.back.packets.*;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.ConnectionProtocol;

import java.util.UUID;

public interface ProxyListener extends ClientboundPacketListener {
    @Override
    default ConnectionProtocol protocol() {
        return ConnectionProtocol.PLAY;
    }

    void addPendingLogin(UUID uuid, Runnable runnable);

    /**
     * handle packets sent by workers
     * @param packet
     */
    void handleWorkerProxyPacket(ProxyBoundContainerPacket packet);

    void handlePlayerTransfer(ProxyBoundPlayerTransferPacket packet);

    void handlePlayerTransferACK(ProxyBoundPlayerTransferACKPacket packet);

    void handleSavePlayerState(ProxyBoundSavePlayerStatePacket packet);

    void handlePlayerInitACK(ProxyBoundPlayerInitACKPacket packet);
}
