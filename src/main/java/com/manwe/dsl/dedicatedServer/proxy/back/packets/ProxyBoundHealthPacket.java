package com.manwe.dsl.dedicatedServer.proxy.back.packets;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import org.jetbrains.annotations.NotNull;

public class ProxyBoundHealthPacket implements Packet<ProxyListener> {

    public static final StreamCodec<FriendlyByteBuf, ProxyBoundHealthPacket> STREAM_CODEC = Packet.codec(
            ProxyBoundHealthPacket::write, ProxyBoundHealthPacket::new
    );

    private final long[] tickTime;
    private final int workerId;

    public ProxyBoundHealthPacket(long[] tickTime, int workerId) {
        this.tickTime = tickTime;
        this.workerId = workerId;
    }

    private ProxyBoundHealthPacket(FriendlyByteBuf buf) {
        this.tickTime = buf.readLongArray();
        this.workerId = buf.readInt();
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeLongArray(this.tickTime);
        buf.writeInt(this.workerId);
    }

    public int getWorkerSource() {
        return workerId;
    }

    public long[] getTickTime() {
        return tickTime;
    }

    @Override
    public @NotNull PacketType<? extends Packet<ProxyListener>> type() {
        return InternalPacketTypes.WORKER_PROXY_HEALTH;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     */
    @Override
    public void handle(ProxyListener pHandler) {
        pHandler.handleWorkerHealth(this);
    }
}