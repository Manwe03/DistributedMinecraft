package com.manwe.dsl.dedicatedServer.worker.listeners;

import com.manwe.dsl.dedicatedServer.worker.packets.*;
import com.manwe.dsl.dedicatedServer.worker.packets.chunkloading.WorkerBoundFakePlayerInformationPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.chunkloading.WorkerBoundFakePlayerLoginPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.chunkloading.WorkerBoundFakePlayerMovePacket;
import com.manwe.dsl.dedicatedServer.worker.packets.login.WorkerBoundRequestLevelInformationPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.login.WorkerBoundPlayerLoginACKPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.login.WorkerBoundPlayerLoginPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerEndTransferPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerTransferPacket;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.game.ServerPacketListener;

import java.util.UUID;

public interface WorkerListener extends ServerPacketListener {

    @Override
    default ConnectionProtocol protocol() {
        return ConnectionProtocol.PLAY;
    }

    void handleProxyWorkerPacket(WorkerBoundContainerPacket packet);

    void handlePlayerLogin(WorkerBoundPlayerLoginPacket packet);

    void handlePlayerLoginACK(WorkerBoundPlayerLoginACKPacket packet);

    void handlePlayerTransfer(WorkerBoundPlayerTransferPacket packet);

    void handleFakePlayerLogin(WorkerBoundFakePlayerLoginPacket packet);

    void handlePlayerDisconnect(WorkerBoundPlayerDisconnectPacket packet);

    void handlePlayerEndTransfer(WorkerBoundPlayerEndTransferPacket packet);

    void handleFakePlayerMove(WorkerBoundFakePlayerMovePacket packet);

    void handleFakePlayerInformation(WorkerBoundFakePlayerInformationPacket packet);

    void handleLevelInformation(WorkerBoundRequestLevelInformationPacket packet);

    Connection getPlayerConnection(UUID playerId);
}
