package com.manwe.dsl.mixin;

import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundSetHealthPacket.class)
public class ClientboundSetHealthPacketMixin {

    @Inject(method = "<init>(FIF)V", at = @At("RETURN"))
    public void onConstruct(float pHealth, int pFood, float pSaturation, CallbackInfo ci){
        //System.out.println("ClientboundSetHealthPacket CREADO");
        //Thread.dumpStack();
    }
}
