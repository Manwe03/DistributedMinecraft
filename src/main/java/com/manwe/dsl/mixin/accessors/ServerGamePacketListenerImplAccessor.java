package com.manwe.dsl.mixin.accessors;

import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.FutureChain;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplAccessor {

    @Accessor("chatMessageChain")
    FutureChain getChatMessageChain();

    @Accessor("signedMessageDecoder")
    SignedMessageChain.Decoder getSignedMessageDecoder();
}
