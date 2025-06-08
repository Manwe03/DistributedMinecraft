package com.manwe.dsl.dedicatedServer.proxy.back.packets;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ProxyBoundSavePlayerStatePacket implements Packet<ProxyListener> {

    private final UUID playerId;
    private final int workerId;
    private final Vec3 position;
    private final String dimension;

    public static final StreamCodec<FriendlyByteBuf, ProxyBoundSavePlayerStatePacket> STREAM_CODEC =
            Packet.codec(ProxyBoundSavePlayerStatePacket::write, ProxyBoundSavePlayerStatePacket::new);

    public ProxyBoundSavePlayerStatePacket(UUID playerId, int workerId, Vec3 position, String dimension){
        this.playerId = playerId;
        this.workerId = workerId;
        this.position = position;
        this.dimension = dimension;
    }

    public ProxyBoundSavePlayerStatePacket(FriendlyByteBuf buf){
        this.playerId = buf.readUUID();
        this.workerId = buf.readInt();
        this.position = new Vec3(buf.readDouble(),buf.readDouble(),buf.readDouble());
        this.dimension = buf.readUtf();
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        buf.writeInt(this.workerId);
        buf.writeDouble(this.position.x);
        buf.writeDouble(this.position.y);
        buf.writeDouble(this.position.z);
        buf.writeUtf(this.dimension);
    }

    public Vec3 getPosition() {
        return position;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getWorkerId() {
        return workerId;
    }

    public String getDimension() {
        return dimension;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     *
     * @param pHandler
     */
    @Override
    public void handle(ProxyListener pHandler) {
        pHandler.handleSavePlayerState(this);
    }

    @Override
    public PacketType<? extends Packet<ProxyListener>> type() {
        return InternalPacketTypes.WORKER_PROXY_SAVE_PLAYER_STATE;
    }
}
