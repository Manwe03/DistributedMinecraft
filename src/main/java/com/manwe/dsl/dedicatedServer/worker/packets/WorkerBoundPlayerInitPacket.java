package com.manwe.dsl.dedicatedServer.worker.packets;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.neoforge.network.connection.ConnectionType;

import java.util.UUID;

public class WorkerBoundPlayerInitPacket implements Packet<WorkerListener> {
    public static final StreamCodec<FriendlyByteBuf, WorkerBoundPlayerInitPacket> STREAM_CODEC = Packet.codec(
            WorkerBoundPlayerInitPacket::write, WorkerBoundPlayerInitPacket::new
    );

    private final GameProfile gameProfile;
    private final ClientInformation clientInformation;

    public WorkerBoundPlayerInitPacket(GameProfile gameProfile, ClientInformation clientInformation) {
        this.gameProfile = gameProfile;
        this.clientInformation = clientInformation;
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.gameProfile.getId());
        buf.writeUtf(this.gameProfile.getName(),255);
        this.clientInformation.write(buf);
    }

    public WorkerBoundPlayerInitPacket(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        String name = buf.readUtf(255);
        this.gameProfile = new GameProfile(uuid,name);
        this.clientInformation = new ClientInformation(buf);
    }

    public ServerPlayer rebuildServerPlayer(MinecraftServer server) {
        return new ServerPlayer(server, server.overworld(), this.gameProfile, this.clientInformation);
    }

    public CommonListenerCookie rebuildCookie(){
        return new CommonListenerCookie(this.gameProfile, 0, this.clientInformation, false, ConnectionType.OTHER);
    }

    @Override
    public PacketType<? extends Packet<WorkerListener>> type() {
        return InternalPacketTypes.PROXY_WORKER_CLIENT_LOGIN;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     */
    @Override
    public void handle(WorkerListener pHandler) {
        System.out.println("Recibido en el handle del packet");
        pHandler.handlePlayerLogin(this);
    }
}
