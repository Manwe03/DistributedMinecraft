package com.manwe.dsl.mixin.accessors;

import io.netty.channel.ChannelFuture;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConnectionListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ServerConnectionListener.class)
public interface ServerConnectionListenerAccessor {

    @Accessor("channels")
    List<ChannelFuture> getChannels();

    @Accessor("server")
    MinecraftServer getServer();

    @Accessor("connections")
    List<Connection> getConnections();

    @Accessor("READ_TIMEOUT")
    int getREAD_TIMEOUT();
}
