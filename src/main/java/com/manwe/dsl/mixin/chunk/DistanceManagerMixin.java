package com.manwe.dsl.mixin.chunk;

import com.manwe.dsl.connectionRouting.RegionRouter;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DistanceManager.class)
public class DistanceManagerMixin {
    /*
    @Redirect(
            method = "addPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/TickingTracker;addTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V")
    )
    public <T> void redirectAddTicket(TickingTracker instance, TicketType<T> pType, ChunkPos pChunkPos, int pTicketLevel, T pKey){
        if(RegionRouter.isChunkInWorkerDomain(pChunkPos.x,pChunkPos.z)){
            instance.addTicket(pType,pChunkPos,pTicketLevel,pKey);
        }
    }*/
}
