package com.manwe.dsl.dedicatedServer.proxy.front;

import com.manwe.dsl.dedicatedServer.CustomDedicatedServer;
import com.manwe.dsl.dedicatedServer.CustomServerConnectionListener;
import com.manwe.dsl.dedicatedServer.proxy.front.listeners.ProxyServerHandshakeListener;
import io.netty.channel.*;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.network.LegacyQueryHandler;

public class ProxyFrontendInit extends ChannelInitializer<Channel> {
    private final CustomDedicatedServer server;
    private final CustomServerConnectionListener listener;

    private static final int READ_TIMEOUT = Integer.parseInt(System.getProperty("neoforge.readTimeout", "30"));

    public ProxyFrontendInit(CustomDedicatedServer server, CustomServerConnectionListener listener) {
        this.server = server;
        this.listener = listener;
    }

    @Override
    protected void initChannel(Channel ch) {
        System.out.println("CUSTOM Initialization");

        try { ch.config().setOption(ChannelOption.TCP_NODELAY, true); } catch (ChannelException ignored) {}
        ChannelPipeline pl = ch.pipeline().addLast("timeout", new ReadTimeoutHandler(READ_TIMEOUT));

        if (server.repliesToStatus()) {
            pl.addLast("legacy_query", new LegacyQueryHandler(server));
        }

        Connection.configureSerialization(pl, PacketFlow.SERVERBOUND, false, null);
        Connection conn = new Connection(PacketFlow.SERVERBOUND);

        listener.getConnections().add(conn); //Add this connection to the connection list for ticking, this ticks the packet listener
        conn.configurePacketHandler(pl);

        conn.setListenerForServerboundHandshake(new ProxyServerHandshakeListener(server, conn));
    }
}