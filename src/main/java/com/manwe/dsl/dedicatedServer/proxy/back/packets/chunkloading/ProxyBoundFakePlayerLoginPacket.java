package com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.BitSet;
import java.util.UUID;

public class ProxyBoundFakePlayerLoginPacket implements Packet<ProxyListener> {

    public static final StreamCodec<FriendlyByteBuf, ProxyBoundFakePlayerLoginPacket> STREAM_CODEC =
            Packet.codec(ProxyBoundFakePlayerLoginPacket::write, ProxyBoundFakePlayerLoginPacket::new);

    private final BitSet workers;
    private final GameProfile gameprofile;
    private final int viewDistance;
    private final ResourceKey<Level> levelResourcekey;
    private final Vec3 pos;

    public ProxyBoundFakePlayerLoginPacket(ServerPlayer player, BitSet workers) {
        this.workers = workers;
        this.gameprofile = player.getGameProfile();
        this.viewDistance = player.requestedViewDistance();
        this.levelResourcekey = player.level().dimension();
        this.pos = player.position();
    }

    public ProxyBoundFakePlayerLoginPacket(FriendlyByteBuf buf) {
        this.workers = buf.readBitSet();
        this.gameprofile = new GameProfile(buf.readUUID(),buf.readUtf());
        this.viewDistance = buf.readInt();
        this.levelResourcekey = buf.readResourceKey(Registries.DIMENSION);
        this.pos = buf.readVec3();
    }
    private void write(FriendlyByteBuf buf) {
        buf.writeBitSet(this.workers);
        buf.writeUUID(gameprofile.getId());
        buf.writeUtf(gameprofile.getName());
        buf.writeInt(this.viewDistance);
        buf.writeResourceKey(this.levelResourcekey);
        buf.writeVec3(this.pos);
    }

    public BitSet getWorkers() {
        return workers;
    }

    public Vec3 getPos() {
        return pos;
    }

    public GameProfile getGameprofile() {
        return gameprofile;
    }

    public ResourceKey<Level> getLevelResourcekey() {
        return levelResourcekey;
    }

    public int getViewDistance() {
        return viewDistance;
    }

    @Override
    public PacketType<? extends Packet<ProxyListener>> type() {
        return InternalPacketTypes.WORKER_PROXY_FAKE_PLAYER_LOGIN;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     *
     * @param pHandler
     */
    @Override
    public void handle(ProxyListener pHandler) {
        pHandler.handleFakePlayerLogin(this);
    }
}
