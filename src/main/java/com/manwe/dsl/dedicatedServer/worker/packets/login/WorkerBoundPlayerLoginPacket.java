package com.manwe.dsl.dedicatedServer.worker.packets.login;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WorkerBoundPlayerLoginPacket implements Packet<WorkerListener> {
    public static final StreamCodec<FriendlyByteBuf, WorkerBoundPlayerLoginPacket> STREAM_CODEC = Packet.codec(
            WorkerBoundPlayerLoginPacket::write, WorkerBoundPlayerLoginPacket::new
    );

    private final GameProfile gameProfile;
    private final ClientInformation clientInformation;

    public WorkerBoundPlayerLoginPacket(GameProfile gameProfile, ClientInformation clientInformation) {
        this.gameProfile = gameProfile;
        this.clientInformation = clientInformation;
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.gameProfile.getId());
        buf.writeUtf(this.gameProfile.getName(),255);
        writePropertyMap(buf, gameProfile.getProperties());

        this.clientInformation.write(buf);
    }

    public WorkerBoundPlayerLoginPacket(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        String name = buf.readUtf(255);
        this.gameProfile = new GameProfile(uuid,name);
        gameProfile.getProperties().putAll(readPropertyMap(buf));

        this.clientInformation = new ClientInformation(buf);
    }

    private static void writePropertyMap(FriendlyByteBuf buf, PropertyMap map) {
        buf.writeVarInt(map.size());
        map.values().forEach(prop -> {
            buf.writeUtf(prop.name());
            buf.writeUtf(prop.value());
            buf.writeBoolean(prop.hasSignature());
            if (prop.hasSignature()) {
                buf.writeUtf(prop.signature());
            }
        });
    }

    private static PropertyMap readPropertyMap(FriendlyByteBuf buf) {
        PropertyMap map = new PropertyMap();
        int total = buf.readVarInt();
        for (int i = 0; i < total; i++) {
            String name  = buf.readUtf();
            String value = buf.readUtf();
            String sig   = buf.readBoolean() ? buf.readUtf() : null;
            map.put(name, new com.mojang.authlib.properties.Property(name, value, sig));
        }
        return map;
    }

    public ServerPlayer rebuildServerPlayer(MinecraftServer server) {
        return new ServerPlayer(server, server.overworld(), this.gameProfile, this.clientInformation);
    }

    public CommonListenerCookie rebuildCookie(){
        return new CommonListenerCookie(this.gameProfile, 0, this.clientInformation, false, ConnectionType.OTHER);
    }

    @Override
    public @NotNull PacketType<? extends Packet<WorkerListener>> type() {
        return InternalPacketTypes.PROXY_WORKER_PLAYER_LOGIN;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     */
    @Override
    public void handle(WorkerListener pHandler) {
        //System.out.println("Handle Login in worker");
        pHandler.handlePlayerLogin(this);
    }
}
