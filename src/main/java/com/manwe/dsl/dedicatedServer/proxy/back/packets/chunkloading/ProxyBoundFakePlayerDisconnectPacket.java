package com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.level.ServerPlayer;

import java.util.BitSet;
import java.util.Set;
import java.util.UUID;

public class ProxyBoundFakePlayerDisconnectPacket implements Packet<ProxyListener> {

    public static final StreamCodec<FriendlyByteBuf, ProxyBoundFakePlayerDisconnectPacket> STREAM_CODEC =
            Packet.codec(ProxyBoundFakePlayerDisconnectPacket::write, ProxyBoundFakePlayerDisconnectPacket::new);

    private final BitSet workers;
    private final UUID playerId;

    public ProxyBoundFakePlayerDisconnectPacket(UUID playerId, BitSet workers) {
        this.playerId = playerId;
        this.workers = workers;
    }

    public ProxyBoundFakePlayerDisconnectPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();
        this.workers = buf.readBitSet();
    }
    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        buf.writeBitSet(this.workers);
    }

    public BitSet getWorkers() {
        return workers;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    @Override
    public PacketType<? extends Packet<ProxyListener>> type() {
        return InternalPacketTypes.WORKER_PROXY_FAKE_PLAYER_DISCONNECT;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     *
     * @param pHandler
     */
    @Override
    public void handle(ProxyListener pHandler) {
        pHandler.handleFakePlayerDisconnect(this);
    }
}
