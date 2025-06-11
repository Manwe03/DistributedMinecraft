package com.manwe.dsl.mixin.log;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {

    @Inject(method = "markChunkPendingToSend(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/chunk/LevelChunk;)V", at=@At("HEAD"))
    private static void markChunkPendingToSend(ServerPlayer pPlayer, LevelChunk pChunk, CallbackInfo ci) {
        //System.out.println("Add Chunk to pending Player["+pPlayer.getName().getString()+"] Pos"+pChunk.getPos());
    }
}
