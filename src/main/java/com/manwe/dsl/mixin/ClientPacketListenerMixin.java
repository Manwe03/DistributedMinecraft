package com.manwe.dsl.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.*;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ONLY FOR DEBUG THIS MOD DOES NOT AIM TO CHANGE THE CLIENT IN ANY WAY
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleChangeDifficulty",at = @At("HEAD"))
    public void handleChangeDifficulty(ClientboundChangeDifficultyPacket par1, CallbackInfo ci){
        System.out.println("handleChangeDifficulty");
    }
    @Inject(method = "handlePlayerAbilities",at = @At("HEAD"))
    public void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket pPacket, CallbackInfo ci){
        System.out.println("handlePlayerAbilities");
    }
    @Inject(method = "handleSetCarriedItem",at = @At("HEAD"))
    public void handleSetCarriedItem(ClientboundSetCarriedItemPacket pPacket, CallbackInfo ci){
        System.out.println("handleSetCarriedItem");
    }
    @Inject(method = "handleUpdateRecipes",at = @At("HEAD"))
    public void handleUpdateRecipes(ClientboundUpdateRecipesPacket pPacket, CallbackInfo ci){
        System.out.println("handleUpdateRecipes");
    }
    @Inject(method = "handleEntityEvent",at = @At("HEAD"))
    public void handleEntityEvent(ClientboundEntityEventPacket pPacket, CallbackInfo ci){
        System.out.println("handleEntityEvent");
    }
    @Inject(method = "handleAddOrRemoveRecipes",at = @At("HEAD"))
    public void handleAddOrRemoveRecipes(ClientboundRecipePacket pPacket, CallbackInfo ci){
        System.out.println("handleAddOrRemoveRecipes");
    }
    @Inject(method = "handleAddObjective",at = @At("HEAD"))
    public void handleAddObjective(ClientboundSetObjectivePacket pPacket, CallbackInfo ci){
        System.out.println("handleAddObjective");
    }
    @Inject(method = "handleSetDisplayObjective",at = @At("HEAD"))
    public void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket pPacket, CallbackInfo ci){
        System.out.println("handleSetDisplayObjective");
    }
    @Inject(method = "handleSetScore",at = @At("HEAD"))
    public void handleSetScore(ClientboundSetScorePacket pPacket, CallbackInfo ci){
        System.out.println("handleSetScore");
    }
    @Inject(method = "handleMovePlayer",at = @At("HEAD"))
    public void handleMovePlayer(ClientboundPlayerPositionPacket pPacket, CallbackInfo ci){
        System.out.println("handleMovePlayer");
    }
    @Inject(method = "handleServerData",at = @At("HEAD"))
    public void handleServerData(ClientboundServerDataPacket pPacket, CallbackInfo ci){
        System.out.println("handleServerData");
    }
    @Inject(method = "handlePlayerInfoUpdate",at = @At("HEAD"))
    public void handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket pPacket, CallbackInfo ci){
        System.out.println("handlePlayerInfoUpdate");
    }
    @Inject(method = "handleInitializeBorder",at = @At("HEAD"))
    public void handleInitializeBorder(ClientboundInitializeBorderPacket pPacket, CallbackInfo ci){
        System.out.println("handleInitializeBorder");
    }
    @Inject(method = "handleSetTime",at = @At("HEAD"))
    public void handleSetTime(ClientboundSetTimePacket pPacket, CallbackInfo ci){
        System.out.println("handleSetTime");
    }
    @Inject(method = "handleSetSpawn",at = @At("HEAD"))
    public void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket pPacket, CallbackInfo ci){
        System.out.println("handleSetSpawn");
    }
    @Inject(method = "handleGameEvent",at = @At("HEAD"))
    public void handleGameEvent(ClientboundGameEventPacket pPacket, CallbackInfo ci){
        System.out.println("handleGameEvent");
    }
    @Inject(method = "handleBossUpdate",at = @At("HEAD"))
    public void handleBossUpdate(ClientboundBossEventPacket pPacket, CallbackInfo ci){
        System.out.println("handleBossUpdate");
    }
    @Inject(method = "handleUpdateMobEffect",at = @At("HEAD"))
    public void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket pPacket, CallbackInfo ci){
        System.out.println("handleUpdateMobEffect");
    }
    @Inject(method = "handleLogin",at = @At("HEAD"))
    public void handleLogin(ClientboundLoginPacket pPacket, CallbackInfo ci){
        System.out.println("handleLogin");
    }
    @Inject(method = "handleSetHealth", at = @At("HEAD"))
    public void handleSetHealth(ClientboundSetHealthPacket pPacket, CallbackInfo ci){
        System.out.println("handleSetHealth");
    }
}
