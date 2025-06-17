package com.manwe.dsl.dedicatedServer.worker.packets;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class WorkerBoundSyncTimePacket implements Packet<WorkerListener> {

    public static final StreamCodec<FriendlyByteBuf, WorkerBoundSyncTimePacket> STREAM_CODEC = Packet.codec(
            WorkerBoundSyncTimePacket::write, WorkerBoundSyncTimePacket::new
    );

    public final Map<String,Long> levelTime;

    public WorkerBoundSyncTimePacket(Map<String,Long> levelTime){
        this.levelTime = levelTime;
    }

    private WorkerBoundSyncTimePacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.levelTime = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = buf.readUtf(2048); //Max Characters?
            long value = buf.readLong();
            this.levelTime.put(key, value);
        }
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeInt(levelTime.size());
        for (Map.Entry<String, Long> entry : levelTime.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeLong(entry.getValue());
        }
    }

    @Override
    public @NotNull PacketType<? extends Packet<WorkerListener>> type() {
        return InternalPacketTypes.PROXY_WORKER_SYNC_TIME;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     */
    @Override
    public void handle(WorkerListener pHandler) {
        pHandler.handleSyncTime(this);
    }
}
