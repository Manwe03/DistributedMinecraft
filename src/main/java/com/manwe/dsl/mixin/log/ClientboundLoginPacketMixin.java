package com.manwe.dsl.mixin.log;

import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ClientboundLoginPacket.class)
public class ClientboundLoginPacketMixin {

    @Inject(method = "<init>(IZLjava/util/Set;IIIZZZLnet/minecraft/network/protocol/game/CommonPlayerSpawnInfo;Z)V",at = @At("RETURN"))
    public void onConstruct(int playerId, boolean hardcore, Set levels, int maxPlayers, int chunkRadius, int simulationDistance, boolean reducedDebugInfo, boolean showDeathScreen, boolean doLimitedCrafting, CommonPlayerSpawnInfo commonPlayerSpawnInfo, boolean enforcesSecureChat, CallbackInfo ci){
        //System.out.println("CONSTRUCT ClientboundLoginPacket "+playerId);
        //Thread.dumpStack();
    }
}
