package com.manwe.dsl.dedicatedServer.worker.listeners;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class WorkerGamePacketListenerImpl extends ServerGamePacketListenerImpl {
    public WorkerGamePacketListenerImpl(MinecraftServer pServer, Connection pConnection, ServerPlayer pPlayer, CommonListenerCookie pCookie) {
        super(pServer, pConnection, pPlayer, pCookie);
    }

    @Override
    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket pPacket) {
        System.out.println("Client accepted Teleport");
        super.handleAcceptTeleportPacket(pPacket);
    }

    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket pPacket) {
        super.handleMovePlayer(pPacket);
    }

    @Override
    public void teleport(double pX, double pY, double pZ, float pYaw, float pPitch) {
        System.out.println("Sent Teleport");
        super.teleport(pX, pY, pZ, pYaw, pPitch);
    }

    @Override
    public void handleKeepAlive(ServerboundKeepAlivePacket pPacket) {
        super.handleKeepAlive(pPacket);
        System.out.println("(NO debería) keep alive recibido en el worker");
    }

    @Override
    protected void keepConnectionAlive() {
        //Desactiva el keep alive del worker, esto lo maneja solo el proxy
        //super.keepConnectionAlive();
    }
}
