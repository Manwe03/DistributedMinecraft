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

    private final long[] mspt;
    private final long[] mem;
    private final int workerId;

    public ProxyBoundHealthPacket(long[] mspt, long[] mem, int workerId) {
        this.mspt = mspt;
        this.mem = mem;
        this.workerId = workerId;
    }

    private ProxyBoundHealthPacket(FriendlyByteBuf buf) {
        this.mspt = buf.readLongArray();
        this.workerId = buf.readInt();
        this.mem = buf.readLongArray();
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeLongArray(this.mspt);
        buf.writeInt(this.workerId);
        buf.writeLongArray(this.mem);
    }

    public int getWorkerSource() {
        return workerId;
    }

    public long[] getMSPTFHistory() {
        return mspt;
    }

    public long[] getMemHistory() {
        return mem;
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