package com.manwe.dsl.dedicatedServer.worker.listeners;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class WorkerGameListener extends ServerGamePacketListenerImpl {
    public WorkerGameListener(MinecraftServer pServer, Connection pConnection, ServerPlayer pPlayer, CommonListenerCookie pCookie) {
        super(pServer, pConnection, pPlayer, pCookie);
    }

    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket pPacket) {
        System.out.println("Received handle move player "+pPacket.getX(0) +":"+ pPacket.getZ(0));
        super.handleMovePlayer(pPacket);
    }
}
