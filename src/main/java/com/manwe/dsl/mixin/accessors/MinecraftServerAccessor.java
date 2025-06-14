package com.manwe.dsl.mixin.accessors;

import com.manwe.dsl.mixinExtension.SetConnectionIntf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConnectionListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MinecraftServer.class, remap = true)
public abstract class MinecraftServerAccessor implements SetConnectionIntf {

    @Shadow @Mutable private ServerConnectionListener connection;

    @Accessor("connection")
    public abstract void setConnection(ServerConnectionListener connection);
}
