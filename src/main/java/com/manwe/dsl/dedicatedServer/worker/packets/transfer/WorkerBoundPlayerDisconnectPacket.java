package com.manwe.dsl.dedicatedServer.worker.packets.transfer;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

import java.util.Optional;
import java.util.UUID;

public class WorkerBoundPlayerDisconnectPacket implements Packet<WorkerListener> {

    public static final StreamCodec<FriendlyByteBuf, WorkerBoundPlayerDisconnectPacket> STREAM_CODEC = Packet.codec(
            WorkerBoundPlayerDisconnectPacket::write, WorkerBoundPlayerDisconnectPacket::new
    );

    private final UUID playerID;
    private final boolean fakePlayer;

    public WorkerBoundPlayerDisconnectPacket(UUID playerID, boolean fakePlayer){
        this.playerID = playerID;
        this.fakePlayer = fakePlayer;
    }

    public WorkerBoundPlayerDisconnectPacket(FriendlyByteBuf buf){
        this.playerID = buf.readUUID();
        this.fakePlayer = buf.readBoolean();
    }

    private void write(FriendlyByteBuf buf){
        buf.writeUUID(this.playerID);
        buf.writeBoolean(this.fakePlayer);
    }

    public UUID getPlayerID(){
        return this.playerID;
    }

    public boolean isFakePlayer(){
        return fakePlayer;
    }

    public DisconnectionDetails getDefaultDisconnectionDetails(){
        return new DisconnectionDetails(Component.translatable("disconnect.disconnected"), Optional.empty(),Optional.empty());
    }

    @Override
    public PacketType<? extends Packet<WorkerListener>> type() {
        return InternalPacketTypes.PROXY_WORKER_PLAYER_DISCONNECT;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     *
     * @param pHandler
     */
    @Override
    public void handle(WorkerListener pHandler) {
        pHandler.handlePlayerDisconnect(this);
    }
}
