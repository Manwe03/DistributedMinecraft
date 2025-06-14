package com.manwe.dsl.dedicatedServer.proxy.back.packets.transfer;

import com.manwe.dsl.connectionRouting.TransientEntityInformation;
import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

import java.util.BitSet;
import java.util.UUID;

public class ProxyBoundPlayerTransferPacket implements Packet<ProxyListener> {

    private final int workerId;
    private final CompoundTag playerNbt;
    private final GameProfile gameProfile;
    private final ClientInformation clientInformation;
    private final TransientEntityInformation entityInformation;
    private final int entityId;
    private final BitSet workers;

    //Tracking view
    private final int viewDistance;

    public static final StreamCodec<FriendlyByteBuf, ProxyBoundPlayerTransferPacket> STREAM_CODEC =
            Packet.codec(ProxyBoundPlayerTransferPacket::write, ProxyBoundPlayerTransferPacket::new);

    public ProxyBoundPlayerTransferPacket(ServerPlayer player, int workerId, BitSet workers){
        this.workerId = workerId;
        this.gameProfile = player.getGameProfile();
        this.clientInformation = player.clientInformation();
        this.playerNbt = new CompoundTag();
        System.out.println("Player being transferred to position X:"+ player.getX()+" Z:"+player.getZ());
        player.saveWithoutId(this.playerNbt);
        this.entityInformation = new TransientEntityInformation(player.getYRot(),player.getXRot());
        this.entityId = player.getId();
        this.workers = workers;
        this.viewDistance = Mth.clamp(player.requestedViewDistance(), 2, player.server.getPlayerList().getViewDistance());
    }

    public ProxyBoundPlayerTransferPacket(FriendlyByteBuf buf) {
        this.workerId = buf.readInt();
        this.playerNbt = buf.readNbt();
        UUID uuid = buf.readUUID();
        String name = buf.readUtf(255);
        this.gameProfile = new GameProfile(uuid,name);
        this.clientInformation = new ClientInformation(buf);
        this.entityInformation = new TransientEntityInformation(buf);
        this.entityId = buf.readInt();
        this.workers = buf.readBitSet();
        this.viewDistance = buf.readInt();
    }
    private void write(FriendlyByteBuf buf) {
        buf.writeInt(this.workerId);
        buf.writeNbt(this.playerNbt);
        buf.writeUUID(this.gameProfile.getId());
        buf.writeUtf(this.gameProfile.getName(),255);
        this.clientInformation.write(buf);
        this.entityInformation.write(buf);
        buf.writeInt(this.entityId);
        buf.writeBitSet(this.workers);
        buf.writeInt(this.viewDistance);
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

    public int getEntityId() {
        return this.entityId;
    }

    public BitSet getWorkers() {
        return workers;
    }

    public int getViewDistance() {
        return viewDistance;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     *
     * @param pHandler
     */
    @Override
    public void handle(ProxyListener pHandler) {
        pHandler.handlePlayerTransfer(this);
    }

    @Override
    public PacketType<? extends Packet<ProxyListener>> type() {
        return InternalPacketTypes.WORKER_PROXY_PLAYER_TRANSFER;
    }
}
