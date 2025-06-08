package com.manwe.dsl.mixin.log;

import com.manwe.dsl.mixin.accessors.PlayerAccessor;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Shadow @Nullable private Pose forcedPose;

    @Shadow protected abstract boolean canPlayerFitWithinBlocksAndEntitiesWhen(Pose pPose);

    @Inject(method = "tick",at= @At("HEAD"))
    public void tick(CallbackInfo ci){
        //System.out.println("TICK - POSE: " + ((Player) (Object)this).getPose().name());
    }
}
