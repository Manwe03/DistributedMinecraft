package com.manwe.dsl.mixin.log;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci){
        //System.out.println("tick()" + ((Player) (Object) this).getName());
        //System.out.flush();
    }

    @Inject(method = "doTick", at = @At("HEAD"))
    public void doTick(CallbackInfo ci) {
        //System.out.println("doTick" + ((Player) (Object) this).getName());
        //System.out.flush();
    }
}
