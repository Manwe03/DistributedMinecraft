package com.manwe.dsl.mixin.invokers;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplInvoker {

    @Invoker("updateAwaitingTeleport")
    boolean invokeUpdateAwaitingTeleport();

    @Invoker("isPlayerCollidingWithAnythingNew")
    boolean invokeIsPlayerCollidingWithAnythingNew(LevelReader pLevel, AABB pBox, double pX, double pY, double pZ);

    @Invoker("noBlocksAround")
    boolean invokeNoBlocksAround(Entity pEntity);
}
