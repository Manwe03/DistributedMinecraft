package com.manwe.dsl.mixin;

import com.manwe.dsl.dedicatedServer.CustomDedicatedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.gui.StatsComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DecimalFormat;


@Mixin(StatsComponent.class)
public class StatsComponentMixin {

    @Shadow @Final private String[] msgs;

    @Shadow @Final private MinecraftServer server;

    @Shadow @Final private static DecimalFormat DECIMAL_FORMAT;

    @Inject(method = "tick", at = @At("HEAD"))
    void tick(CallbackInfo ci){
        if(this.server instanceof CustomDedicatedServer dedicatedServer){
            Float mspt = dedicatedServer.workersMeanMSPT.get(1);
            if(mspt != null) this.msgs[2] =  "Avg tick: " + DECIMAL_FORMAT.format(mspt) + " ms [w1]";
            mspt = dedicatedServer.workersMeanMSPT.get(2);
            if(mspt != null) this.msgs[3] =  "Avg tick: " + DECIMAL_FORMAT.format(mspt) + " ms [w2]";
            mspt = dedicatedServer.workersMeanMSPT.get(3);
            if(mspt != null) this.msgs[4] =  "Avg tick: " + DECIMAL_FORMAT.format(mspt) + " ms [w3]";
            mspt = dedicatedServer.workersMeanMSPT.get(4);
            if(mspt != null) this.msgs[5] =  "Avg tick: " + DECIMAL_FORMAT.format(mspt) + " ms [w4]";
            mspt = dedicatedServer.workersMeanMSPT.get(5);
            if(mspt != null) this.msgs[6] =  "Avg tick: " + DECIMAL_FORMAT.format(mspt) + " ms [w5]";
            mspt = dedicatedServer.workersMeanMSPT.get(6);
            if(mspt != null) this.msgs[7] =  "Avg tick: " + DECIMAL_FORMAT.format(mspt) + " ms [w6]";
            mspt = dedicatedServer.workersMeanMSPT.get(7);
            if(mspt != null) this.msgs[8] =  "Avg tick: " + DECIMAL_FORMAT.format(mspt) + " ms [w7]";
            mspt = dedicatedServer.workersMeanMSPT.get(8);
            if(mspt != null) this.msgs[9] =  "Avg tick: " + DECIMAL_FORMAT.format(mspt) + " ms [w8]";
            mspt = dedicatedServer.workersMeanMSPT.get(9);
            if(mspt != null) this.msgs[10] = "Avg tick: " + DECIMAL_FORMAT.format(mspt) + " ms [w9]";
        }
    }
}
