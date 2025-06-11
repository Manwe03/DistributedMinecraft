package com.manwe.dsl.dedicatedServer.worker.listeners;

import net.minecraft.Util;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class WorkerFakePlayerListenerImpl extends ServerGamePacketListenerImpl {

    private final WorkerListenerImpl workerListener;

    public WorkerFakePlayerListenerImpl(MinecraftServer pServer, Connection pConnection, ServerPlayer pPlayer, CommonListenerCookie pCookie, WorkerListenerImpl workerListener) {
        super(pServer, pConnection, pPlayer, pCookie);
        this.workerListener = workerListener;
    }

    /*
    @Override
    public void tick() {
        //this.player.doTick();
        super.tick(); //TODO ver si se puede desactivar
    }
    */

    @Override
    protected void keepConnectionAlive() {
        //Desactiva el keep alive del worker, esto lo maneja solo el proxy
    }
}
