package com.manwe.dsl.mixin.log;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(method = "placeNewPlayer",at = @At("HEAD"))
    public void placeNewPlayer(Connection pConnection, ServerPlayer pPlayer, CommonListenerCookie pCookie, CallbackInfo ci) {
        System.out.println("placeNewPlayer VANILLA ESTO NO DEBERA APARECER EN NINGÚN CASO");
    }

    @Inject(method = "remove", at = @At("HEAD"))
    public void removePlayer(ServerPlayer pPlayer, CallbackInfo ci){
        System.out.println("PlayerList REMOVE");
        //Thread.dumpStack();
    }
}
