package com.manwe.dsl.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Shadow private Component tabListDisplayName;

    @Shadow @Nullable private BlockPos respawnPosition;

    @Shadow private boolean hasTabListName;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci){
        //System.out.println("Tick al jugador");
        //System.out.println("GameMode: "+gameMode.getGameModeForPlayer().getName());
    }

    @Inject(method = "doTick", at = @At("HEAD"))
    public void doTick(CallbackInfo ci) {
        //System.out.println("Do Tick"+(this.hasTabListName ? this.tabListDisplayName : ""));
    }
}
