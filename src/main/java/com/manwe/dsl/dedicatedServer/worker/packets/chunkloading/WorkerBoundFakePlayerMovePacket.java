package com.manwe.dsl.dedicatedServer.worker.packets.chunkloading;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerMovePacket;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class WorkerBoundFakePlayerMovePacket implements Packet<WorkerListener> {

    public static final StreamCodec<FriendlyByteBuf, WorkerBoundFakePlayerMovePacket> STREAM_CODEC =
            Packet.codec(WorkerBoundFakePlayerMovePacket::write, WorkerBoundFakePlayerMovePacket::new);

    private final UUID playerId;
    private final Vec3 pos;

    public WorkerBoundFakePlayerMovePacket(ProxyBoundFakePlayerMovePacket packet) {
        this.playerId = packet.getPlayerId();
        this.pos = packet.getPos();
    }

    public WorkerBoundFakePlayerMovePacket(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();
        this.pos = new Vec3(buf.readInt(),buf.readInt(),buf.readInt());
    }
    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        buf.writeInt((int) pos.x);
        buf.writeInt((int) pos.y);
        buf.writeInt((int) pos.z);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Vec3 getPos() {
        return pos;
    }

    @Override
    public PacketType<? extends Packet<WorkerListener>> type() {
        return InternalPacketTypes.PROXY_WORKER_FAKE_PLAYER_MOVE;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     *
     * @param pHandler
     */
    @Override
    public void handle(WorkerListener pHandler) {
        pHandler.handleFakePlayerMove(this);
    }
}
