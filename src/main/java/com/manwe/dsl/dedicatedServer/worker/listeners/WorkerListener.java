package com.manwe.dsl.dedicatedServer.worker.listeners;

import com.manwe.dsl.dedicatedServer.proxy.back.packets.WorkerBoundPlayerInitPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.WorkerBoundPlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.ProxyWorkerPacket;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.game.ServerPacketListener;

import java.util.UUID;

public interface WorkerListener extends ServerPacketListener {

    @Override
    default ConnectionProtocol protocol() {
        return ConnectionProtocol.PLAY;
    }

    void handleProxyWorkerPacket(ProxyWorkerPacket packet);

    void handlePlayerLogin(WorkerBoundPlayerInitPacket packet);

    void handlePlayerDisconnect(WorkerBoundPlayerDisconnectPacket packet);

    Connection getPlayerConnection(UUID playerId);
}
