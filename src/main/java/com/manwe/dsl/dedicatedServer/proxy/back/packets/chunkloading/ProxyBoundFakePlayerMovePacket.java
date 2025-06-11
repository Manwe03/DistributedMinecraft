package com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.phys.Vec3;

import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ProxyBoundFakePlayerMovePacket implements Packet<ProxyListener> {

    public static final StreamCodec<FriendlyByteBuf, ProxyBoundFakePlayerMovePacket> STREAM_CODEC =
            Packet.codec(ProxyBoundFakePlayerMovePacket::write, ProxyBoundFakePlayerMovePacket::new);

    private final UUID playerId;
    private final Vec3 pos;
    private final BitSet workers;

    public ProxyBoundFakePlayerMovePacket(UUID playerId, Vec3 pos, BitSet workers) {
        this.playerId = playerId;
        this.pos = pos;
        this.workers = workers;
    }

    public ProxyBoundFakePlayerMovePacket(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();
        this.pos = new Vec3(buf.readInt(),buf.readInt(),buf.readInt());
        this.workers = buf.readBitSet();
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        buf.writeInt((int) pos.x);
        buf.writeInt((int) pos.y);
        buf.writeInt((int) pos.z);
        buf.writeBitSet(this.workers);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Vec3 getPos() {
        return pos;
    }

    public BitSet getWorkers() {
        return workers;
    }

    @Override
    public PacketType<? extends Packet<ProxyListener>> type() {
        return InternalPacketTypes.WORKER_PROXY_FAKE_PLAYER_MOVE;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     *
     * @param pHandler
     */
    @Override
    public void handle(ProxyListener pHandler) {
        pHandler.handleFakePlayerMove(this);
    }
}
