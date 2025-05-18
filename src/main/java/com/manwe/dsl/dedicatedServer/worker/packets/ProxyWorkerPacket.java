package com.manwe.dsl.dedicatedServer.worker.packets;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

import java.util.UUID;

public class ProxyWorkerPacket implements Packet<WorkerListener> {

    private final UUID playerId;

    public static final StreamCodec<FriendlyByteBuf, ProxyWorkerPacket> STREAM_CODEC = Packet.codec(
            ProxyWorkerPacket::write, ProxyWorkerPacket::new
    );

    public ProxyWorkerPacket(UUID playerId){
        this.playerId = playerId;
    }

    private ProxyWorkerPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     *
     * @param pHandler
     */
    @Override
    public void handle(WorkerListener pHandler) {
        System.out.println("Worker: Handle by Packet - UUID " + this.playerId);
    }

    @Override
    public PacketType<? extends Packet<WorkerListener>> type() {
        return InternalPacketTypes.PROXY_WORKER_PACKET_CONTAINER;
    }
}
