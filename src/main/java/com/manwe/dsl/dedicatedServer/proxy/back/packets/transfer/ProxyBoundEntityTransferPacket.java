package com.manwe.dsl.dedicatedServer.proxy.back.packets.transfer;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ProxyBoundEntityTransferPacket implements Packet<ProxyListener> {

    public static final StreamCodec<FriendlyByteBuf, ProxyBoundEntityTransferPacket> STREAM_CODEC =
            Packet.codec(ProxyBoundEntityTransferPacket::write, ProxyBoundEntityTransferPacket::new);

    private final CompoundTag entityNbt;
    private final int entityLocalId;

    private final int workerId;

    public ProxyBoundEntityTransferPacket(Entity entity, int workerId) {
        this.entityNbt = new CompoundTag();
        entity.save(entityNbt); //Save id del registro de tipo no la id local
        entityNbt.putString("Dimension",entity.level().dimension().location().toString());
        this.workerId = workerId;
        this.entityLocalId = entity.getId();
    }

    public ProxyBoundEntityTransferPacket(FriendlyByteBuf buf) {
        this.entityNbt = buf.readNbt();
        this.entityLocalId = buf.readInt();
        this.workerId = buf.readInt();
    }
    private void write(FriendlyByteBuf buf) {
        buf.writeNbt(this.entityNbt);
        buf.writeInt(this.entityLocalId);
        buf.writeInt(this.workerId);
    }

    public int getWorkerId() {
        return workerId;
    }

    public CompoundTag getEntityNbt() {
        return entityNbt;
    }

    public int getEntityLocalId() {
        return entityLocalId;
    }

    @Override
    public @NotNull PacketType<? extends Packet<ProxyListener>> type() {
        return InternalPacketTypes.WORKER_PROXY_ENTITY_TRANSFER;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     *
     * @param pHandler
     */
    @Override
    public void handle(ProxyListener pHandler) {
        pHandler.handleEntityTrasnfer(this);
    }
}
