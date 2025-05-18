package com.manwe.dsl.dedicatedServer.proxy.back.packets;

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

import java.util.UUID;

public class PlayerInitPacket implements Packet<WorkerListener> {
    public static final StreamCodec<FriendlyByteBuf, PlayerInitPacket> STREAM_CODEC = Packet.codec(
            PlayerInitPacket::write, PlayerInitPacket::new
    );

    private final GameProfile gameProfile;
    private final ClientInformation clientInformation;

    public PlayerInitPacket(GameProfile gameProfile, ClientInformation clientInformation) {
        this.gameProfile = gameProfile;
        this.clientInformation = clientInformation;
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.gameProfile.getId());
        buf.writeUtf(this.gameProfile.getName(),255);
        this.clientInformation.write(buf);
    }

    public PlayerInitPacket(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        String name = buf.readUtf(255);
        this.gameProfile = new GameProfile(uuid,name);
        this.clientInformation = new ClientInformation(buf);
    }

    public ServerPlayer rebuildServerPlayer(MinecraftServer server) {
        return new ServerPlayer(server, server.overworld(), this.gameProfile, this.clientInformation);
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
