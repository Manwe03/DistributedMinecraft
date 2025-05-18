package com.manwe.dsl.dedicatedServer.worker.listeners;

import com.manwe.dsl.dedicatedServer.proxy.back.packets.PlayerInitPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.ProxyWorkerPacket;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class WorkerListenerImpl implements WorkerListener{

    private final MinecraftServer server;
    private final Connection connection;

    public WorkerListenerImpl(MinecraftServer server, Connection connection) {
        this.server = server;
        this.connection = connection;
    }

    @Override
    public void handleWrappedPacket(ProxyWorkerPacket packet) {
        System.out.println("Mensaje del Proxy");
    }

    @Override
    public void handlePlayerLogin(PlayerInitPacket packet) {
        System.out.println("capturado handlePlayerLogin en el loginListener");

        //TODO hay que ver esto de las dimensiones
        ServerPlayer player = packet.rebuildServerPlayer(server);
        System.out.println(player.getDisplayName() + " Connected");

        System.out.println("player"+ player.getDisplayName().getString());
        //server.getPlayerList().placeNewPlayer(connection, player, new CommonListenerCookie(player.getGameProfile(),0, player.clientInformation(), false));
    }

    @Override
    public void onDisconnect(DisconnectionDetails pDetails) {

    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}
