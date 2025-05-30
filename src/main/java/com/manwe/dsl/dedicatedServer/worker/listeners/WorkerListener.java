package com.manwe.dsl.dedicatedServer.worker.listeners;

import com.manwe.dsl.dedicatedServer.worker.packets.WorkerBoundPlayerInitPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.WorkerBoundPlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.WorkerBoundContainerPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.WorkerBoundPlayerTransferPacket;
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

    void handlePlayerLogin(WorkerBoundPlayerInitPacket packet);

    void handlePlayerTransfer(WorkerBoundPlayerTransferPacket packet);

    void handlePlayerDisconnect(WorkerBoundPlayerDisconnectPacket packet);

    Connection getPlayerConnection(UUID playerId);
}
