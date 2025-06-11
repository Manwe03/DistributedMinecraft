package com.manwe.dsl.mixin.invokers;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerChunkSender.class)
public interface PlayerChunkSenderInvoker {

    @Invoker("sendChunk")
    static void sendChunk(ServerGamePacketListenerImpl pPacketListener, ServerLevel pLevel, LevelChunk pChunk){
        throw new AssertionError("No execute");
    }
}
