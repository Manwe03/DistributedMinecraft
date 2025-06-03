package com.manwe.dsl.dedicatedServer.proxy.back.packets;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

import java.util.UUID;

public class ProxyBoundPlayerInitACKPacket implements Packet<ProxyListener> {

    private final UUID playerId; //Player successfully transferred

    public static final StreamCodec<FriendlyByteBuf, ProxyBoundPlayerInitACKPacket> STREAM_CODEC =
            Packet.codec(ProxyBoundPlayerInitACKPacket::write, ProxyBoundPlayerInitACKPacket::new);

    public ProxyBoundPlayerInitACKPacket(UUID playerId) {
        this.playerId = playerId;
    }

    public ProxyBoundPlayerInitACKPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();
    }
    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
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
        pHandler.handlePlayerInitACK(this);
    }

    @Override
    public PacketType<? extends Packet<ProxyListener>> type() {
        return InternalPacketTypes.WORKER_PROXY_PLAYER_INIT_ACK;
    }
}
