package com.manwe.dsl.dedicatedServer.worker.packets.chunkloading;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerInformationPacket;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

import java.util.UUID;

public class WorkerBoundFakePlayerInformationPacket implements Packet<WorkerListener> {
    public static final StreamCodec<FriendlyByteBuf, WorkerBoundFakePlayerInformationPacket> STREAM_CODEC =
            Packet.codec(WorkerBoundFakePlayerInformationPacket::write, WorkerBoundFakePlayerInformationPacket::new);

    private final UUID playerId;
    private final int viewDistance;
    
    public WorkerBoundFakePlayerInformationPacket(ProxyBoundFakePlayerInformationPacket packet) {
        this.viewDistance = packet.getViewDistance();
        this.playerId = packet.getPlayerId();
    }

    public WorkerBoundFakePlayerInformationPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();
        this.viewDistance = buf.readInt();
    }
    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        buf.writeInt(this.viewDistance);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getViewDistance() {
        return viewDistance;
    }

    @Override
    public PacketType<? extends Packet<WorkerListener>> type() {
        return InternalPacketTypes.PROXY_WORKER_FAKE_PLAYER_INFORMATION;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     *
     * @param pHandler
     */
    @Override
    public void handle(WorkerListener pHandler) {
        pHandler.handleFakePlayerInformation(this);
    }
}
