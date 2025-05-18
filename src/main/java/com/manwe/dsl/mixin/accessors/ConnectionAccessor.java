package com.manwe.dsl.mixin.accessors;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Connection.class, remap = true)
public interface ConnectionAccessor {

    @Accessor("channel")
    void setChannel(Channel ch);

    @Accessor("packetListener")
    void setPacketListener(PacketListener listener);

}
