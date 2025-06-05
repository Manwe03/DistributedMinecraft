package com.manwe.dsl.mixin.log;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
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

    @Inject(method = "addEntity",at = @At("HEAD"))
    public void addEntity(Entity pEntity, CallbackInfo ci){
        System.out.println("addEntity: "+ pEntity.getName().getString() + " id:" + pEntity.getId());
    }

    @Inject(method = "removeEntity",at = @At("HEAD"))
    public void removeEntity(int pEntityId, Entity.RemovalReason pReason, CallbackInfo ci){
        if(Minecraft.getInstance().player == null) return;
        int me = Minecraft.getInstance().player.getId();

        if(pEntityId == me) {
            System.out.println("ELIMINADO JUGADOR "+ pReason.name());
            Thread.dumpStack();
        }
    }
}
