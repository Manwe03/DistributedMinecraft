package com.manwe.dsl.mixin.log;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AmbientSoundHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Shadow protected abstract boolean isControlledCamera();

    @Inject(method = "tick",at = @At("HEAD"))
    private void tick(CallbackInfo ci){
        //System.out.println("TICK");
        /*
        if (((Entity)(Object)this).level().hasChunkAt(((Entity)(Object)this).getBlockX(), ((Entity)(Object)this).getBlockZ())) {

            if (((Entity)(Object)this).isPassenger()) {
                System.out.println("IsPassenger");
            } else {
                System.out.println("SendPosition");
            }
        } else {
            System.out.println("Not send Position");
        }*/
    }

    @Inject(method = "sendPosition", at = @At("HEAD"))
    private void sendPosition(CallbackInfo ci){
        /*
        if (this.isControlledCamera()) {
            System.out.println("Send Movement");
        }else {
            System.out.println("Not Send Movement, isControlledCamera false");
        }*/
    }
}
