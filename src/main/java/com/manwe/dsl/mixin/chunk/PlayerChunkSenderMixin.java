package com.manwe.dsl.mixin.chunk;

import com.manwe.dsl.dedicatedServer.worker.chunk.ChunkLoadingFakePlayer;
import com.manwe.dsl.mixin.invokers.PlayerChunkSenderInvoker;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlayerChunkSender.class)
public abstract class PlayerChunkSenderMixin {

    @Shadow private int unacknowledgedBatches;

    @Shadow private int maxUnacknowledgedBatches;

    @Shadow private float desiredChunksPerTick;

    @Shadow private float batchQuota;

    @Shadow @Final private LongSet pendingChunks;

    @Shadow protected abstract List<LevelChunk> collectChunksToSend(ChunkMap pChunkMap, ChunkPos pChunkPos);

    /**
     * @author Manwe
     * @reason Auto respond to batch if this is a fake player
     */
    @Overwrite
    public void sendNextChunks(ServerPlayer pPlayer) {
        if (this.unacknowledgedBatches < this.maxUnacknowledgedBatches) {
            float f = Math.max(1.0F, this.desiredChunksPerTick);
            this.batchQuota = Math.min(this.batchQuota + this.desiredChunksPerTick, f);
            if (!(this.batchQuota < 1.0F)) {
                if (!this.pendingChunks.isEmpty()) {
                    ServerLevel serverlevel = pPlayer.serverLevel();
                    ChunkMap chunkmap = serverlevel.getChunkSource().chunkMap;
                    List<LevelChunk> list = this.collectChunksToSend(chunkmap, pPlayer.chunkPosition());
                    if (!list.isEmpty()) {
                        //System.out.println("sendNextChunks");

                        ServerGamePacketListenerImpl servergamepacketlistenerimpl = pPlayer.connection;
                        this.unacknowledgedBatches++;
                        servergamepacketlistenerimpl.send(ClientboundChunkBatchStartPacket.INSTANCE);

                        for (LevelChunk levelchunk : list) {
                            PlayerChunkSenderInvoker.sendChunk(servergamepacketlistenerimpl, serverlevel, levelchunk);
                        }

                        servergamepacketlistenerimpl.send(new ClientboundChunkBatchFinishedPacket(list.size()));
                        this.batchQuota = this.batchQuota - (float)list.size();

                        //New
                        if(pPlayer instanceof ChunkLoadingFakePlayer){ //Respond immediately as if batch was received
                            //System.out.println("Send internally onChunkBatchReceivedByClient");
                            ((PlayerChunkSender)(Object)this).onChunkBatchReceivedByClient(10);
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "sendChunk", at=@At("HEAD"))
    private static void sendChunkLog(ServerGamePacketListenerImpl pPacketListener, ServerLevel pLevel, LevelChunk pChunk, CallbackInfo ci){
        //System.out.println("SendChunk Pos"+pChunk.getPos());
    }

    @Inject(method = "collectChunksToSend", at=@At("RETURN"))
    private void collectChunksToSendLog(ChunkMap pChunkMap, ChunkPos pChunkPos, CallbackInfoReturnable<List<LevelChunk>> cir) {
        //System.out.println("collectChunksToSend size: "+cir.getReturnValue().size());
    }
}
