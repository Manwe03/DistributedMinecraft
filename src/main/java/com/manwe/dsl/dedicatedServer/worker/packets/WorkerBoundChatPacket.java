package com.manwe.dsl.dedicatedServer.worker.packets;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record WorkerBoundChatPacket(String message, String playerName, UUID playerId)
        implements Packet<WorkerListener> {

    public static final StreamCodec<FriendlyByteBuf, WorkerBoundChatPacket> STREAM_CODEC = Packet.codec(
            WorkerBoundChatPacket::write, WorkerBoundChatPacket::new
    );

    private WorkerBoundChatPacket(FriendlyByteBuf buf) {
        this(buf.readUtf(256), buf.readUtf(256), buf.readUUID());
        //this(buf.readJsonWithCodec(PlayerChatMessage.MAP_CODEC.codec()), buf.readUtf(256), buf.readUtf(256), buf.readUUID());

    }

    /**
     * Writes the raw packet data to the data stream.
     */
    private void write(FriendlyByteBuf buf) {
        //buf.writeJsonWithCodec(PlayerChatMessage.MAP_CODEC.codec(), playerChatMessage);
        buf.writeUtf(this.message,256);
        buf.writeUtf(this.playerName,256);
        buf.writeUUID(this.playerId);
    }

    @Override
    public @NotNull PacketType<? extends Packet<WorkerListener>> type() {
        return InternalPacketTypes.PROXY_WORKER_CHAT_MESSAGE;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     */
    @Override
    public void handle(WorkerListener pHandler) {
        pHandler.handleRemoteChatMessage(this);
    }
}