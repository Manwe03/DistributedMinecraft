package com.manwe.dsl.dedicatedServer.proxy;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.arbiter.ArbiterClient;
import com.manwe.dsl.connectionRouting.RegionRouter;
import com.manwe.dsl.mixin.accessors.ServerConnectionListenerAccessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.RateKickingConnection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.LegacyQueryHandler;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetAddress;

public class ProxyServerConnectionListener extends ServerConnectionListener {

    /**
     * Router se encarga de almacenar la topología del sistema quien es proxy y quien es worker además de la lista de workers
     */
    RegionRouter router = new RegionRouter();

    public ProxyServerConnectionListener(MinecraftServer pServer) {
        super(pServer);
    }

    /**
     * Adds a channel that listens on publicly accessible network ports
     */
    public void startTcpServerListener(@Nullable InetAddress pAddress, int pPort, ArbiterClient.ArbiterRes topology) throws IOException {
        System.out.println("Proxy - startTcpServerListener");
        router.iniRouter(topology.port,topology.proxy,topology.connections); //Router Initialization Set global System Info

        if (pAddress == null) pAddress = new java.net.InetSocketAddress(pPort).getAddress();
        net.neoforged.neoforge.network.DualStackUtils.checkIPv6(pAddress);
        synchronized (((ServerConnectionListenerAccessor)this).getChannels()) {
            Class<? extends ServerSocketChannel> oclass;
            EventLoopGroup eventloopgroup;
            if (Epoll.isAvailable() && ((ServerConnectionListenerAccessor)this).getServer().isEpollEnabled()) {
                oclass = EpollServerSocketChannel.class;
                eventloopgroup = SERVER_EPOLL_EVENT_GROUP.get();
                DistributedServerLevels.LOGGER.info("Using epoll channel type");
            } else {
                oclass = NioServerSocketChannel.class;
                eventloopgroup = SERVER_EVENT_GROUP.get();
                DistributedServerLevels.LOGGER.info("Using default channel type");
            }

            ChannelInitializer<Channel> initializer = router.isProxy() //Si el router pertenece a un Server proxy
                ? new ProxyFrontendInit(((ServerConnectionListenerAccessor) this).getServer(), ((ServerConnectionListenerAccessor) this).getConnections())
                : vanillaInit();   // copia literal del initChannel que usa Mojang

            ((ServerConnectionListenerAccessor) this).getChannels().add(
                new ServerBootstrap()
                    .group(eventloopgroup)
                    .channel(oclass)
                    .childHandler(initializer)
                    .localAddress(pAddress, pPort)
                    .bind()
                    .syncUninterruptibly()
            );
        }
    }

    private ChannelInitializer<Channel> vanillaInit(){
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) {
                try {
                    ch.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException channelexception) {
                }

                ChannelPipeline channelpipeline = ch.pipeline().addLast("timeout", new ReadTimeoutHandler(((ServerConnectionListenerAccessor) ProxyServerConnectionListener.this).getREAD_TIMEOUT()));
                if (((ServerConnectionListenerAccessor) ProxyServerConnectionListener.this).getServer().repliesToStatus()) {
                    channelpipeline.addLast("legacy_query", new LegacyQueryHandler(((ServerConnectionListenerAccessor) ProxyServerConnectionListener.this).getServer()));
                }

                Connection.configureSerialization(channelpipeline, PacketFlow.SERVERBOUND, false, null);
                int i = ((ServerConnectionListenerAccessor) ProxyServerConnectionListener.this).getServer().getRateLimitPacketsPerSecond();
                Connection connection = (Connection) (i > 0 ? new RateKickingConnection(i) : new Connection(PacketFlow.SERVERBOUND));
                ((ServerConnectionListenerAccessor) ProxyServerConnectionListener.this).getConnections().add(connection);
                connection.configurePacketHandler(channelpipeline);
                connection.setListenerForServerboundHandshake(new ServerHandshakePacketListenerImpl(((ServerConnectionListenerAccessor) ProxyServerConnectionListener.this).getServer(), connection));
            }
        };
    }
}
