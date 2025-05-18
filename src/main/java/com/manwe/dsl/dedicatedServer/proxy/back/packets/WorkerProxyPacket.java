package com.manwe.dsl.dedicatedServer.proxy.back.packets;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class WorkerProxyPacket implements Packet<ProxyListener> {

    public static final StreamCodec<FriendlyByteBuf, WorkerProxyPacket> STREAM_CODEC = Packet.codec(
            WorkerProxyPacket::write, WorkerProxyPacket::new
    );

    public WorkerProxyPacket(FriendlyByteBuf buf) {
    }

    private void write(FriendlyByteBuf buf) {

    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     * @param pHandler
     */
    @Override
    public void handle(ProxyListener pHandler) {
        System.out.println("Proxy: Handle by packet");
    }

    @Override
    public PacketType<? extends Packet<ProxyListener>> type() {
        return InternalPacketTypes.WORKER_PROXY_PACKET_CONTAINER;
    }
}
