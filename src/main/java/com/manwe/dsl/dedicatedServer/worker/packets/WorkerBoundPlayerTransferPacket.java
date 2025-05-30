package com.manwe.dsl.dedicatedServer.worker.packets;

import com.manwe.dsl.connectionRouting.TransientEntityInformation;
import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.ProxyBoundPlayerTransferPacket;
import com.manwe.dsl.dedicatedServer.worker.LocalPlayerList;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.neoforge.network.connection.ConnectionType;

import java.util.Objects;
import java.util.UUID;

public class WorkerBoundPlayerTransferPacket implements Packet<WorkerListener> {

    private final int workerId;
    private final CompoundTag playerNbt;
    private final GameProfile gameProfile;
    private final ClientInformation clientInformation;
    private final TransientEntityInformation entityInformation;

    public static final StreamCodec<FriendlyByteBuf, WorkerBoundPlayerTransferPacket> STREAM_CODEC =
            Packet.codec(WorkerBoundPlayerTransferPacket::write, WorkerBoundPlayerTransferPacket::new);

    public WorkerBoundPlayerTransferPacket(ProxyBoundPlayerTransferPacket packet){
        this.workerId = packet.getWorkerId();
        this.gameProfile = packet.getGameProfile();
        this.clientInformation = packet.getClientInformation();
        this.playerNbt = packet.getPlayerNbt();
        this.entityInformation = packet.getEntityInformation();
    }

    public WorkerBoundPlayerTransferPacket(FriendlyByteBuf buf) {
        this.workerId = buf.readInt();
        this.playerNbt = buf.readNbt();
        //GameProfile
        UUID uuid = buf.readUUID();
        String name = buf.readUtf(255);
        this.gameProfile = new GameProfile(uuid,name);
        //ClientInformation
        this.clientInformation = new ClientInformation(buf);
        //TransientEntityInformation
        this.entityInformation = new TransientEntityInformation(buf);
    }
    private void write(FriendlyByteBuf buf) {
        buf.writeInt(this.workerId);
        buf.writeNbt(this.playerNbt);
        //GameProfile
        buf.writeUUID(this.gameProfile.getId());
        buf.writeUtf(this.gameProfile.getName(),255);
        //ClientInformation
        this.clientInformation.write(buf);
        //TransientEntityInformation
        this.entityInformation.write(buf);
    }

    public ClientInformation getClientInformation() {
        return clientInformation;
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public CompoundTag getPlayerNbt() {
        return playerNbt;
    }

    public int getWorkerId() {
        return workerId;
    }

    public TransientEntityInformation getEntityInformation() {
        return entityInformation;
    }

    public ServerPlayer rebuildServerPlayer(MinecraftServer server) {
        return new ServerPlayer(server, server.overworld(), this.gameProfile, this.clientInformation);
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
