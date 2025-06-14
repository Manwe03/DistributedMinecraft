package com.manwe.dsl.dedicatedServer.worker.packets.transfer;

import com.manwe.dsl.connectionRouting.TransientEntityInformation;
import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.transfer.ProxyBoundPlayerTransferPacket;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.neoforge.network.connection.ConnectionType;

import java.util.BitSet;
import java.util.Optional;
import java.util.UUID;

public class WorkerBoundPlayerTransferPacket implements Packet<WorkerListener> {

    private final CompoundTag playerNbt;
    private final GameProfile gameProfile;
    private final ClientInformation clientInformation;
    private final int entityId;
    private final BitSet workers;

    //Tracking view
    private final int viewDistance;

    public static final StreamCodec<FriendlyByteBuf, WorkerBoundPlayerTransferPacket> STREAM_CODEC =
            Packet.codec(WorkerBoundPlayerTransferPacket::write, WorkerBoundPlayerTransferPacket::new);

    public WorkerBoundPlayerTransferPacket(ProxyBoundPlayerTransferPacket packet){
        this.gameProfile = packet.getGameProfile();
        this.clientInformation = packet.getClientInformation();
        this.playerNbt = packet.getPlayerNbt();
        this.entityId = packet.getEntityId();
        this.workers = packet.getWorkers();
        this.viewDistance = packet.getViewDistance();
    }

    public WorkerBoundPlayerTransferPacket(FriendlyByteBuf buf) {
        this.playerNbt = buf.readNbt();
        //GameProfile
        UUID uuid = buf.readUUID();
        String name = buf.readUtf(255);
        this.gameProfile = new GameProfile(uuid,name);
        //ClientInformation
        this.clientInformation = new ClientInformation(buf);

        this.entityId = buf.readInt();

        this.workers = buf.readBitSet();

        this.viewDistance = buf.readInt();
    }
    private void write(FriendlyByteBuf buf) {
        buf.writeNbt(this.playerNbt);
        //GameProfile
        buf.writeUUID(this.gameProfile.getId());
        buf.writeUtf(this.gameProfile.getName(),255);
        //ClientInformation
        this.clientInformation.write(buf);

        buf.writeInt(this.entityId);

        buf.writeBitSet(this.workers);

        buf.writeInt(this.viewDistance);
    }

    public CompoundTag getPlayerNbt() {
        return playerNbt;
    }

    public BitSet getWorkers() {
        return workers;
    }

    public DisconnectionDetails getDefaultDisconnectionDetails(){
        return new DisconnectionDetails(Component.translatable("disconnect.disconnected"), Optional.empty(),Optional.empty());
    }

    public ServerPlayer rebuildServerPlayer(MinecraftServer server) {
        ServerPlayer clone = new ServerPlayer(server, server.overworld(), this.gameProfile, this.clientInformation);
        clone.setId(this.entityId); //Clone the id assigned by its source worker, entity id do not collide between workers each worker has its own id range
        clone.setChunkTrackingView(new ChunkTrackingView.Positioned(clone.chunkPosition(),viewDistance)); //Set this view distance to avoid chunk resends
        clone.load(this.playerNbt);
        return clone;
    }

    public CommonListenerCookie rebuildCookie(){
        return new CommonListenerCookie(this.gameProfile, 0, this.clientInformation, false, ConnectionType.OTHER);
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     *
     * @param pHandler
     */
    @Override
    public void handle(WorkerListener pHandler) {
        pHandler.handlePlayerTransfer(this);
    }

    @Override
    public PacketType<? extends Packet<WorkerListener>> type() {
        return InternalPacketTypes.PROXY_WORKER_PLAYER_TRANSFER;
    }
}
