package com.manwe.dsl.mixin.log;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.entity.EntityTickList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Shadow @Final private EntityTickList tickingEntities;

    @Inject(method = "tickEntities",at = @At("HEAD"))
    public void tickEntities(CallbackInfo ci){
        //System.out.println("Tick entities");
        //this.tickingEntities.forEach(System.out::println);
    }
}
