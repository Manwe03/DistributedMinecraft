package com.manwe.dsl.dedicatedServer.worker.listeners;

import com.manwe.dsl.dedicatedServer.proxy.back.packets.PlayerInitPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.ProxyWorkerPacket;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.game.ServerPacketListener;

public interface WorkerListener extends ServerPacketListener {

    @Override
    default ConnectionProtocol protocol() {
        return ConnectionProtocol.PLAY;
    }

    void handleWrappedPacket(ProxyWorkerPacket packet);
    void handlePlayerLogin(PlayerInitPacket packet);
}
