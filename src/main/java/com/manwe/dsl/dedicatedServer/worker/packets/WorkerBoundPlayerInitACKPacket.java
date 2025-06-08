package com.manwe.dsl.dedicatedServer.worker.packets;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

import java.util.UUID;

public class WorkerBoundPlayerInitACKPacket implements Packet<WorkerListener> {

    private final UUID playerId;

    public static final StreamCodec<FriendlyByteBuf, WorkerBoundPlayerInitACKPacket> STREAM_CODEC = Packet.codec(
            WorkerBoundPlayerInitACKPacket::write, WorkerBoundPlayerInitACKPacket::new
    );

    public WorkerBoundPlayerInitACKPacket(UUID playerId) {
        this.playerId = playerId;
    }

    public WorkerBoundPlayerInitACKPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    @Override
    public PacketType<? extends Packet<WorkerListener>> type() {
        return InternalPacketTypes.PROXY_WORKER_PLAYER_INIT_ACK;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     */
    @Override
    public void handle(WorkerListener pHandler) {
        pHandler.handlePlayerLoginACK(this);
    }
}
