package com.manwe.dsl.mixin.chunk;

import com.manwe.dsl.config.DSLServerConfigs;
import com.manwe.dsl.connectionRouting.RegionRouter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {

    @Shadow @Final public ServerLevel level;

    @Inject(method = "getChunk", at = @At("HEAD"), cancellable = true)
    public void getChunk(int pX, int pZ, ChunkStatus pChunkStatus, boolean pRequireChunk, CallbackInfoReturnable<ChunkAccess> cir){
        if (!RegionRouter.isChunkInWorkerDomain(pX, pZ)) {
            cir.setReturnValue(pRequireChunk ? makeEmpty(this.level,pX,pZ) : null);
        }
    }

    @Inject(method = "getChunkNow", at = @At("HEAD"), cancellable = true)
    public void getChunkNow(int pChunkX, int pChunkZ, CallbackInfoReturnable<LevelChunk> cir) {
        if (!RegionRouter.isChunkInWorkerDomain(pChunkX, pChunkZ)) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "getChunkFutureMainThread", at = @At("HEAD"), cancellable = true)
    private void getChunkFutureMainThread(int pX, int pZ, ChunkStatus pChunkStatus, boolean pRequireChunk, CallbackInfoReturnable<CompletableFuture<ChunkResult<ChunkAccess>>> cir) {
        if (!RegionRouter.isChunkInWorkerDomain(pX, pZ)) {
            cir.setReturnValue(GenerationChunkHolder.UNLOADED_CHUNK_FUTURE);
        }
    }

    private static ChunkAccess makeEmpty(ServerLevel level, int chunkX, int chunkZ) {
        Holder<Biome> plainsBiome = level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
        return new EmptyLevelChunk(level, new ChunkPos(chunkX, chunkZ), plainsBiome);
    }
}
