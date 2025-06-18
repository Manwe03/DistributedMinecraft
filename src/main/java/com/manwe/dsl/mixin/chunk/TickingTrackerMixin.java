package com.manwe.dsl.mixin.chunk;

import com.manwe.dsl.connectionRouting.RegionRouter;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import net.minecraft.server.level.TickingTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TickingTracker.class)
public class TickingTrackerMixin {

    @Shadow @Final protected Long2ByteMap chunks;

    /**
     * @author Manwe
     * @reason Return unloaded level for chunks outside this worker
     */
    @Overwrite
    protected int getLevel(long pChunkPos) {
        if(RegionRouter.isChunkOutsideWorkerDomain(pChunkPos)){
            return this.chunks.defaultReturnValue();
        } else {
            return this.chunks.get(pChunkPos);
        }
    }
}