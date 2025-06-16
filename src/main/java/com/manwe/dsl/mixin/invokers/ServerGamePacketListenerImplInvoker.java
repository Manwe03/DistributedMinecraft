package com.manwe.dsl.mixin.invokers;

import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplInvoker {

    @Invoker("tryHandleChat")
    void invokeTryHandleChat(String pMessage, Runnable pHandler);

    @Invoker("getSignedMessage")
    PlayerChatMessage invokeGetSignedMessage(ServerboundChatPacket pPacket, LastSeenMessages pLastSeenMessages) throws SignedMessageChain.DecodeException;

    @Invoker("handleMessageDecodeFailure")
    void invokeHandleMessageDecodeFailure(SignedMessageChain.DecodeException pException);

    @Invoker("filterTextPacket")
    CompletableFuture<FilteredText> invokeFilterTextPacket(String pText);

    @Invoker("unpackAndApplyLastSeen")
    Optional<LastSeenMessages> invokeUnpackAndApplyLastSeen(LastSeenMessages.Update pUpdate);
}
