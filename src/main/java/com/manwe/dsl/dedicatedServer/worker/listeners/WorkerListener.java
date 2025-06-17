package com.manwe.dsl.dedicatedServer.worker.listeners;

import com.manwe.dsl.dedicatedServer.worker.packets.*;
import com.manwe.dsl.dedicatedServer.worker.packets.chunkloading.WorkerBoundFakePlayerInformationPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.chunkloading.WorkerBoundFakePlayerLoginPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.chunkloading.WorkerBoundFakePlayerMovePacket;
import com.manwe.dsl.dedicatedServer.worker.packets.login.WorkerBoundRequestLevelInformationPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.login.WorkerBoundPlayerLoginPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundEntityTransferPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerEndTransferPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerTransferPacket;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.game.ServerPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface WorkerListener extends ServerPacketListener {

    @Override
    default @NotNull ConnectionProtocol protocol() {
        return ConnectionProtocol.PLAY;
    }

    void handleProxyWorkerPacket(WorkerBoundContainerPacket packet);

    void handlePlayerLogin(WorkerBoundPlayerLoginPacket packet);

    void handlePlayerTransfer(WorkerBoundPlayerTransferPacket packet);

    void handleFakePlayerLogin(WorkerBoundFakePlayerLoginPacket packet);

    void handlePlayerDisconnect(WorkerBoundPlayerDisconnectPacket packet);

    void handlePlayerEndTransfer(WorkerBoundPlayerEndTransferPacket packet);

    void handleFakePlayerMove(WorkerBoundFakePlayerMovePacket packet);

    void handleFakePlayerInformation(WorkerBoundFakePlayerInformationPacket packet);

    void handleLevelInformation(WorkerBoundRequestLevelInformationPacket packet);

    void handleEntityTransfer(WorkerBoundEntityTransferPacket packet);

    void handleRemoteChatMessage(WorkerBoundChatPacket packet);

    /**
     * Proxy requested to update time value in multiple levels
     */
    void handleSyncTime(WorkerBoundSyncTimePacket packet);

    /**
     * Handle Sync Request,sends this worker day time for each level
     */
    void handleReqSyncTime(WorkerBoundReqSyncTimePacket packet);

    Connection getPlayerConnection(UUID playerId);
}
