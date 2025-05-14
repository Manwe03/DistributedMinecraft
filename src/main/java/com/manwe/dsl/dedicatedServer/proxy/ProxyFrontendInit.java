package com.manwe.dsl.dedicatedServer.proxy;

import com.manwe.dsl.connectionRouting.RegionRouter;
import com.manwe.dsl.dedicatedServer.proxy.listeners.ProxyHandshakeListener;
import io.netty.channel.*;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;

import java.util.List;

class ProxyFrontendInit extends ChannelInitializer<Channel> {
    private final MinecraftServer server;
    private final List<Connection> tracker;   // misma lista que usa el core

    private static final int READ_TIMEOUT = Integer.parseInt(System.getProperty("neoforge.readTimeout", "30"));

    ProxyFrontendInit(MinecraftServer srv, List<Connection> t) {
        this.server = srv;
        this.tracker = t;
    }

    @Override
    protected void initChannel(Channel ch) {
        System.out.println("CUSTOM Initialization");

        try { ch.config().setOption(ChannelOption.TCP_NODELAY, true); } catch (ChannelException ignored) {}
        ChannelPipeline pl = ch.pipeline().addLast("timeout", new ReadTimeoutHandler(READ_TIMEOUT));

        Connection.configureSerialization(pl, PacketFlow.SERVERBOUND, false, null);
        Connection conn = new Connection(PacketFlow.SERVERBOUND);
        tracker.add(conn);
        conn.configurePacketHandler(pl);

        conn.setListenerForServerboundHandshake(new ProxyHandshakeListener(server, conn));
    }
}