package com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

import java.util.BitSet;
import java.util.UUID;

public class ProxyBoundFakePlayerInformationPacket implements Packet<ProxyListener> {

    public static final StreamCodec<FriendlyByteBuf, ProxyBoundFakePlayerInformationPacket> STREAM_CODEC =
            Packet.codec(ProxyBoundFakePlayerInformationPacket::write, ProxyBoundFakePlayerInformationPacket::new);

    private final BitSet workers;
    private final UUID playerId;
    private final int viewDistance;

    public ProxyBoundFakePlayerInformationPacket(UUID playerId, BitSet workers, int viewDistance) {
        this.playerId = playerId;
        this.workers = workers;
        this.viewDistance = viewDistance;
    }

    public ProxyBoundFakePlayerInformationPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();
        this.workers = buf.readBitSet();
        this.viewDistance = buf.readInt();
    }
    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        buf.writeBitSet(this.workers);
        buf.writeInt(this.viewDistance);
    }

    public BitSet getWorkers() {
        return workers;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getViewDistance() {
        return viewDistance;
    }

    @Override
    public PacketType<? extends Packet<ProxyListener>> type() {
        return InternalPacketTypes.WORKER_PROXY_FAKE_PLAYER_INFORMATION;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     *
     * @param pHandler
     */
    @Override
    public void handle(ProxyListener pHandler) {
        pHandler.handleFakePlayerInformation(this);
    }
}
