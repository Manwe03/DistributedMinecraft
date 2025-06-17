package com.manwe.dsl.dedicatedServer.proxy.back.listeners;

import com.manwe.dsl.dedicatedServer.proxy.back.packets.*;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerInformationPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerLoginPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerMovePacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.login.ProxyBoundLevelInformationPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.transfer.ProxyBoundEntityTransferPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.transfer.ProxyBoundPlayerTransferACKPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.transfer.ProxyBoundPlayerTransferPacket;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.ConnectionProtocol;
import org.jetbrains.annotations.NotNull;

public interface ProxyListener extends ClientboundPacketListener {
    @Override
    default @NotNull ConnectionProtocol protocol() {
        return ConnectionProtocol.PLAY;
    }

    /**
     * handle packets sent by workers
     */
    void handleWorkerProxyPacket(ProxyBoundContainerPacket packet);

    void handlePlayerTransfer(ProxyBoundPlayerTransferPacket packet);

    void handlePlayerTransferACK(ProxyBoundPlayerTransferACKPacket packet);

    void handleSavePlayerState(ProxyBoundSavePlayerStatePacket packet);

    void handleFakePlayerLogin(ProxyBoundFakePlayerLoginPacket packet);

    void handleFakePlayerMove(ProxyBoundFakePlayerMovePacket packet);

    void handleFakePlayerDisconnect(ProxyBoundFakePlayerDisconnectPacket packet);

    void handleFakePlayerInformation(ProxyBoundFakePlayerInformationPacket packet);

    void handleLevelInformation(ProxyBoundLevelInformationPacket packet);

    void handleEntityTrasnfer(ProxyBoundEntityTransferPacket packet);

    /**
     * Handles worker time of day responses
     * Sends message to be compared with other workers
     */
    void handleSyncTime(ProxyBoundSyncTimePacket packet);
}
