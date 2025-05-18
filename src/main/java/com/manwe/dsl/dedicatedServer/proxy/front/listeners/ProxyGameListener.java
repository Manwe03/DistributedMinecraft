package com.manwe.dsl.dedicatedServer.proxy.front.listeners;

import com.manwe.dsl.connectionRouting.RegionRouter;
import com.manwe.dsl.dedicatedServer.proxy.WorkerTunnel;
import com.manwe.dsl.dedicatedServer.proxy.ProxyDedicatedServer;
import com.manwe.dsl.dedicatedServer.worker.packets.ProxyWorkerPacket;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class ProxyGameListener extends ServerGamePacketListenerImpl {

    //Direct pre-casted reference
    ProxyDedicatedServer server;
    RegionRouter router;

    public ProxyGameListener(MinecraftServer pServer, Connection pConnection, ServerPlayer pPlayer, CommonListenerCookie pCookie, RegionRouter router) {
        super(pServer, pConnection, pPlayer, pCookie);
        if(pServer instanceof ProxyDedicatedServer server1){
            this.server = server1;
            this.router = router;
        }else {
            throw new RuntimeException("ProxyGameListener not initialized from a ProxyDedicatedServer");
        }
    }

    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket pPacket) {
        System.out.println("Move: " + pPacket.getX(0) + ":" + pPacket.getY(0) + ":" + pPacket.getZ(0));

        WorkerTunnel tunnel = router.route(0,0); //Select tunnel
        tunnel.send(new ProxyWorkerPacket(player.getUUID())); //Send wrapper
    }
}
