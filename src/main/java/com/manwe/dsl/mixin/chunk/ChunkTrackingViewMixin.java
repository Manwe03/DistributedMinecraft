package com.manwe.dsl.mixin.chunk;

import com.manwe.dsl.connectionRouting.RegionRouter;
import net.minecraft.server.level.ChunkTrackingView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkTrackingView.class)
public interface ChunkTrackingViewMixin {

    @Inject(method = "isWithinDistance", at = @At("RETURN"), cancellable = true)
    private static void isWithinDistance(int pCenterX, int pCenterZ, int pViewDistance, int pX, int pZ, boolean pIncludeOuterChunksAdjacentToViewBorder, CallbackInfoReturnable<Boolean> cir) {
        //In chunk cords, pX pZ
        if(RegionRouter.isChunkOutsideWorkerDomain(pX, pZ)) {
            cir.setReturnValue(false); //Set false if outside worker bounds
        }
    }
}
