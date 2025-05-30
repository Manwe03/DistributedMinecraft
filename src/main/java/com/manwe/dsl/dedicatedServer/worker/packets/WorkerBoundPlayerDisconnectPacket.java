package com.manwe.dsl.dedicatedServer.worker.packets;

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

    public WorkerBoundPlayerDisconnectPacket(UUID playerID){
        this.playerID = playerID;
    }

    public WorkerBoundPlayerDisconnectPacket(FriendlyByteBuf buf){
        this.playerID = buf.readUUID();
    }

    private void write(FriendlyByteBuf buf){
        buf.writeUUID(this.playerID);
    }

    public UUID getPlayerID(){
        return this.playerID;
    }

    public DisconnectionDetails getDefaultDisconnectionDetails(){
        return new DisconnectionDetails(Component.translatable("disconnect.disconnected"), Optional.empty(),Optional.empty());
    }

    @Override
    public PacketType<? extends Packet<WorkerListener>> type() {
        return InternalPacketTypes.PROXY_WORKER_CLIENT_DISCONNECT;
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
