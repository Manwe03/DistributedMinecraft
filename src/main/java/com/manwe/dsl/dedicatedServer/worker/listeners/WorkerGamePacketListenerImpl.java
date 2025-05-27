package com.manwe.dsl.dedicatedServer.worker.listeners;

import com.manwe.dsl.mixin.accessors.ServerGamePacketListenerImplAccessor;
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
        //System.out.println("Worker>> handleAcceptTeleportPacket, ID: "+pPacket.getId());
        //System.out.println("Pre handleAcceptTeleportPacket: " + ((ServerGamePacketListenerImplAccessor)this).getAwaitingPositionFromClient());
        super.handleAcceptTeleportPacket(pPacket);
        //System.out.println("Pos handleAcceptTeleportPacket: " + ((ServerGamePacketListenerImplAccessor)this).getAwaitingPositionFromClient());
        //System.out.flush();
    }

    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket pPacket) {
        //System.out.println("handleMovePlayer: " + pPacket.getX(0) + ":" + pPacket.getY(0) + ":" + pPacket.getZ(0));
        //System.out.flush();
        super.handleMovePlayer(pPacket);
    }

    @Override
    public void teleport(double pX, double pY, double pZ, float pYaw, float pPitch) {
        //System.out.println("Worker>> teleport: " + pX +":"+ pY +":"+ pZ);
        //System.out.flush();
        super.teleport(pX, pY, pZ, pYaw, pPitch);
    }

    @Override
    public void handleKeepAlive(ServerboundKeepAlivePacket pPacket) {
        super.handleKeepAlive(pPacket);
        System.out.println("(NO debería) keep alive recibido en el worker");
    }

    @Override
    public void tick() {
        //System.out.println("Tick! tickCount=" + ((ServerGamePacketListenerImplAccessor)this).getTickCount() + " player=" + player.getName());
        super.tick();
    }

    @Override
    protected void keepConnectionAlive() {
        //Desactiva el keep alive del worker, esto lo maneja solo el proxy
        //super.keepConnectionAlive();
    }
}
