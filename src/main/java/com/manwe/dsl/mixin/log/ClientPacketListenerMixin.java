package com.manwe.dsl.mixin.log;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
        System.out.println("handleEntityEvent id:" + pPacket.getEventId());
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
        pPacket.actions().forEach(action -> System.out.println(action));

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
    @Inject(method = "handleLevelChunkWithLight", at = @At("HEAD"))
    public void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket pPacket, CallbackInfo ci){
        System.out.printf("chunk %d,%d arrived (player %d,%d)%n",
                pPacket.getX(), pPacket.getZ(),
                Minecraft.getInstance().player.getBlockX() >> 4,
                Minecraft.getInstance().player.getBlockZ() >> 4);
    }
    @Inject(method = "handleChunkBatchFinished", at = @At("HEAD"))
    public void handleChunkBatchFinished(ClientboundChunkBatchFinishedPacket pPacket, CallbackInfo ci){
        System.out.println("handleChunkBatchFinished size: "+pPacket.batchSize());
    }
    @Inject(method = "handleChunkBatchStart", at = @At("HEAD"))
    public void handleChunkBatchStart(ClientboundChunkBatchStartPacket pPacket, CallbackInfo ci){
        System.out.println("handleChunkBatchStart");
    }

    @Inject(method = "handleForgetLevelChunk", at = @At("HEAD"))
    public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket pPacket, CallbackInfo ci){
        System.out.printf("chunk %d,%d UNLOADED%n", pPacket.pos().x, pPacket.pos().z);
    }

    @Inject(method = "handleRemoveEntities", at = @At("HEAD"))
    public void handleRemoveEntities(ClientboundRemoveEntitiesPacket pPacket, CallbackInfo ci){
        /*
        int me = Minecraft.getInstance().player.getId();
        System.out.println("handleRemoveEntities player id " + me);

        if(Minecraft.getInstance().level == null) {
            System.out.println("level == null");
        } else {
            for(Integer id : pPacket.getEntityIds()){
                Entity entity = ((ClientLevelInvoker) Minecraft.getInstance().level).invokeGetEntities().get(id);
                if(entity != null){
                    System.out.println("Removed entity: " + entity.getName().getString());
                } else {
                    System.out.println("Removed unknown with id: " +id);
                }
            }
        }
        */
    }
    @Inject(method = "handleRespawn", at = @At("HEAD"))
    public void handleRespawn(ClientboundRespawnPacket pPacket, CallbackInfo ci){
        System.out.println("handleRespawn");
    }
    @Inject(method = "handlePlayerInfoRemove", at = @At("HEAD"))
    public void handlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket pPacket, CallbackInfo ci){
        System.out.println("handlePlayerInfoRemove UUIDS:");
        pPacket.profileIds().forEach(System.out::println);
    }

}
