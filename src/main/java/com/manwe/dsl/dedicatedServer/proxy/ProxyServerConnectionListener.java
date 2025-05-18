package com.manwe.dsl.dedicatedServer.proxy;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.dedicatedServer.proxy.front.ProxyFrontendInit;
import com.manwe.dsl.dedicatedServer.worker.WorkerFrontendInit;
import com.manwe.dsl.mixin.accessors.ServerConnectionListenerAccessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetAddress;

public class ProxyServerConnectionListener extends ServerConnectionListener {

    public ProxyServerConnectionListener(MinecraftServer pServer) {
        super(pServer);
    }

    /**
     * Adds a channel that listens on publicly accessible network ports
     */
    public void startTcpServerListener(@Nullable InetAddress pAddress, int pPort) throws IOException {
        System.out.println("Proxy - startTcpServerListener");
        if(!(this.getServer() instanceof ProxyDedicatedServer server)) throw new RuntimeException("ProxyServerConnectionListener not called from a ProxyDedicatedServer");

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

            ChannelInitializer<Channel> initializer = server.isProxy()
                ? new ProxyFrontendInit((ProxyDedicatedServer) this.getServer(), this)
                : new WorkerFrontendInit((ProxyDedicatedServer) this.getServer(),this);

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
}
