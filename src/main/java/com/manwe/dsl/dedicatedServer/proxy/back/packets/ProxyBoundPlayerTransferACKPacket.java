package com.manwe.dsl.dedicatedServer.proxy.back.packets;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

import java.util.UUID;

public class ProxyBoundPlayerTransferACKPacket implements Packet<ProxyListener> {

    private final int workerId; //Owner of this packet
    private final UUID playerId; //Player successfully transferred

    public static final StreamCodec<FriendlyByteBuf, ProxyBoundPlayerTransferACKPacket> STREAM_CODEC =
            Packet.codec(ProxyBoundPlayerTransferACKPacket::write, ProxyBoundPlayerTransferACKPacket::new);

    public ProxyBoundPlayerTransferACKPacket(int workerId, UUID playerId) {
        this.workerId = workerId;
        this.playerId = playerId;
    }

    public ProxyBoundPlayerTransferACKPacket(FriendlyByteBuf buf) {
        this.workerId = buf.readInt();
        this.playerId = buf.readUUID();
    }
    private void write(FriendlyByteBuf buf) {
        buf.writeInt(this.workerId);
        buf.writeUUID(this.playerId);
    }

    public int getWorkerId() {
        return workerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     *
     * @param pHandler
     */
    @Override
    public void handle(ProxyListener pHandler) {
        pHandler.handlePlayerTransferACK(this);
    }

    @Override
    public PacketType<? extends Packet<ProxyListener>> type() {
        return InternalPacketTypes.WORKER_PROXY_PLAYER_TRANSFER_ACK;
    }
}
