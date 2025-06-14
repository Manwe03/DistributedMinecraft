package com.manwe.dsl.dedicatedServer.worker.packets.transfer;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import com.manwe.dsl.mixin.ServerLevelMixin;
import com.manwe.dsl.mixinExtension.ServerLevelExtension;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class WorkerBoundEntityTransferPacket implements Packet<WorkerListener> {

    public static final StreamCodec<FriendlyByteBuf, WorkerBoundEntityTransferPacket> STREAM_CODEC =
            Packet.codec(WorkerBoundEntityTransferPacket::write, WorkerBoundEntityTransferPacket::new);

    private final CompoundTag entityNbt;
    private final int entityLocalId;

    public WorkerBoundEntityTransferPacket(CompoundTag entityNbt, int entityLocalId) {
        this.entityNbt = entityNbt;
        this.entityLocalId = entityLocalId;
    }

    public WorkerBoundEntityTransferPacket(FriendlyByteBuf buf) {
        this.entityNbt = buf.readNbt();
        this.entityLocalId = buf.readInt();
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeNbt(this.entityNbt);
        buf.writeInt(this.entityLocalId);
    }

    public void placeEntity(MinecraftServer server){

        String dimStr = entityNbt.getString("Dimension");
        ResourceLocation dimRL = ResourceLocation.parse(dimStr);
        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimRL);

        ServerLevel level = Optional.ofNullable(server.getLevel(dimKey)).orElse(server.overworld());

        /*
        ResourceKey<Level> key = DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, this.entityNbt.get("Dimension"))).resultOrPartial(DistributedServerLevels.LOGGER::error).orElse(Level.OVERWORLD);
        ServerLevel level = Objects.requireNonNullElseGet(server.getLevel(key), server::overworld); //Fallback → Overlord
        */
        
        Entity clone = EntityType.loadEntityRecursive(this.entityNbt, level, Function.identity());
        if (clone == null) {
            DistributedServerLevels.LOGGER.warn("Transferred entity was null, skipped");
            return;
        }

        clone.setId(this.entityLocalId);
        ((ServerLevelExtension) level).distributedServerLevels$addEntityWithoutEvent(clone);
    }

    @Override
    public @NotNull PacketType<? extends Packet<WorkerListener>> type() {
        return InternalPacketTypes.PROXY_WORKER_ENTITY_TRANSFER;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     *
     * @param pHandler
     */
    @Override
    public void handle(WorkerListener pHandler) {
        pHandler.handleEntityTransfer(this);
    }
}
