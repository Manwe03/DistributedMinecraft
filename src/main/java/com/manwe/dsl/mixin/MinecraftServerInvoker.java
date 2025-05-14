package com.manwe.dsl.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = MinecraftServer.class, remap = true)
public interface MinecraftServerInvoker {


    @Invoker("setInitialSpawn")
    static void invokeSetInitialSpawn(ServerLevel pLevel, ServerLevelData pLevelData, boolean pGenerateBonusChest, boolean pDebug) {
        throw new AssertionError("setInitialSpawn - this code is not executed");
    }
}
