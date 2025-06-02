package com.manwe.dsl.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Shadow @Final public ServerPlayerGameMode gameMode;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci){
        //System.out.println("Tick al jugador");
        System.out.println("GameMode: "+gameMode.getGameModeForPlayer().getName());
    }
}
