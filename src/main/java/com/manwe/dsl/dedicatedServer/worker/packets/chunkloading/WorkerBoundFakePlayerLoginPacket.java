package com.manwe.dsl.dedicatedServer.worker.packets.chunkloading;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerLoginPacket;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class WorkerBoundFakePlayerLoginPacket implements Packet<WorkerListener> {

    public static final StreamCodec<FriendlyByteBuf, WorkerBoundFakePlayerLoginPacket> STREAM_CODEC =
            Packet.codec(WorkerBoundFakePlayerLoginPacket::write, WorkerBoundFakePlayerLoginPacket::new);

    public final GameProfile gameprofile;
    public final ResourceKey<Level> levelResourcekey;
    public final int viewDistance;
    public final Vec3 pos;

    public WorkerBoundFakePlayerLoginPacket(ProxyBoundFakePlayerLoginPacket packet) {
        this.gameprofile = packet.getGameprofile();
        this.levelResourcekey = packet.getLevelResourcekey();
        this.viewDistance = packet.getViewDistance();
        this.pos = packet.getPos();
    }

    public WorkerBoundFakePlayerLoginPacket(FriendlyByteBuf buf) {
        this.gameprofile = new GameProfile(buf.readUUID(),buf.readUtf());
        this.levelResourcekey = buf.readResourceKey(Registries.DIMENSION);
        this.viewDistance = buf.readInt();
        this.pos = buf.readVec3();
    }
    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(gameprofile.getId());
        buf.writeUtf(gameprofile.getName());
        buf.writeResourceKey(this.levelResourcekey);
        buf.writeInt(this.viewDistance);
        buf.writeVec3(this.pos);
    }

    @Override
    public PacketType<? extends Packet<WorkerListener>> type() {
        return InternalPacketTypes.PROXY_WORKER_FAKE_PLAYER_LOGIN;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     *
     * @param pHandler
     */
    @Override
    public void handle(WorkerListener pHandler) {
        pHandler.handleFakePlayerLogin(this);
    }
}
