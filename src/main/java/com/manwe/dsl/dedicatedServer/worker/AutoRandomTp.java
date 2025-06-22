package com.manwe.dsl.dedicatedServer.worker;


import com.manwe.dsl.config.DSLServerConfigs;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.concurrent.ThreadLocalRandom;

public class AutoRandomTp {
    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event){
        if (DSLServerConfigs.AUTO_TP.get() && event.getEntity() instanceof ServerPlayer sp) {
            double a = -DSLServerConfigs.RANGE.get();
            double b = DSLServerConfigs.RANGE.get();
            double mu    = (a + b) / 2.0;
            double sigma = (b - a) / 5.152;

            double x;
            double z;
            do {
                x = ThreadLocalRandom.current().nextGaussian() * sigma + mu;
                z = ThreadLocalRandom.current().nextGaussian() * sigma + mu;
            } while (x < a || x > b || z < a || z > b);

            System.out.println("Auto Teleport "+sp.getDisplayName().getString()+" to X:"+x+" Z:"+z);

            //Trigger event
            net.neoforged.neoforge.event.entity.EntityTeleportEvent.TeleportCommand transferEvent = net.neoforged.neoforge.event.EventHooks.onEntityTeleportCommand(sp, x, sp.getY(), z);
            if(!transferEvent.isCanceled()){
                sp.teleportTo(x,sp.getY(),z);
            }
        }
    }
}
