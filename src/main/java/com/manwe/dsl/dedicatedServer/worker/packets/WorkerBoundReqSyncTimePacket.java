package com.manwe.dsl.dedicatedServer.worker.packets;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import org.jetbrains.annotations.NotNull;

public class WorkerBoundReqSyncTimePacket implements Packet<WorkerListener> {
    public static final StreamCodec<FriendlyByteBuf, WorkerBoundReqSyncTimePacket> STREAM_CODEC = Packet.codec(
            WorkerBoundReqSyncTimePacket::write, WorkerBoundReqSyncTimePacket::new
    );

    public WorkerBoundReqSyncTimePacket(){}

    private WorkerBoundReqSyncTimePacket(FriendlyByteBuf buf) {}

    private void write(FriendlyByteBuf buf) {}

    @Override
    public @NotNull PacketType<? extends Packet<WorkerListener>> type() {
        return InternalPacketTypes.PROXY_WORKER_REQ_SYNC_TIME;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     */
    @Override
    public void handle(WorkerListener pHandler) {
        pHandler.handleReqSyncTime(this);
    }
}
