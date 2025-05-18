package com.manwe.dsl.dedicatedServer.worker;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerGameListener;
import com.manwe.dsl.mixin.accessors.PlayerListAccessor;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.PlayerDataStorage;

import java.util.Optional;
import java.util.UUID;

public class LocalPlayerList extends DedicatedPlayerList {
    public LocalPlayerList(DedicatedServer pServer, LayeredRegistryAccess<RegistryLayer> pRegistries, PlayerDataStorage pPlayerIo) {
        super(pServer, pRegistries, pPlayerIo);
    }

    @Override
    public void placeNewPlayer(Connection pConnection, ServerPlayer pPlayer, CommonListenerCookie pCookie) {

        Optional<CompoundTag> optional1 = this.load(pPlayer);
        ResourceKey<Level> resourcekey = optional1.<ResourceKey<Level>>flatMap(p_337568_ -> DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, p_337568_.get("Dimension"))).resultOrPartial(DistributedServerLevels.LOGGER::error)).orElse(Level.OVERWORLD);
        ServerLevel serverlevel = this.getServer().getLevel(resourcekey);
        ServerLevel serverlevel1;
        if (serverlevel == null) {
            DistributedServerLevels.LOGGER.warn("Unknown respawn dimension {}, defaulting to overworld", resourcekey);
            serverlevel1 = this.getServer().overworld();
        } else {
            serverlevel1 = serverlevel;
        }
        pPlayer.setServerLevel(serverlevel1);
        String s1 = pConnection.getLoggableAddress(this.getServer().logIPs());
        DistributedServerLevels.LOGGER.info(
                "{}[{}] logged in with entity id {} at ({}, {}, {})",
                pPlayer.getName().getString(),
                s1,
                pPlayer.getId(),
                pPlayer.getX(),
                pPlayer.getY(),
                pPlayer.getZ()
        );

        pPlayer.loadGameTypes(optional1.orElse(null));
        ServerGamePacketListenerImpl servergamepacketlistenerimpl = new WorkerGameListener(this.getServer(), pConnection, pPlayer, pCookie);
        pConnection.setupInboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(this.getServer().registryAccess(), servergamepacketlistenerimpl.getConnectionType())), servergamepacketlistenerimpl);

        System.out.println("setupInboundProtocol");

        pPlayer.getStats().markAllDirty();

        this.updateEntireScoreboard(serverlevel1.getScoreboard(), pPlayer);
        this.getServer().invalidateStatus();

        servergamepacketlistenerimpl.teleport(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), pPlayer.getYRot(), pPlayer.getXRot());

        this.getPlayers().add(pPlayer);
        ((PlayerListAccessor)this).getPlayersByUUID().put(pPlayer.getUUID(), pPlayer);

        serverlevel1.addNewPlayer(pPlayer);

        this.getServer().getCustomBossEvents().onPlayerConnect(pPlayer);

        if (optional1.isPresent() && optional1.get().contains("RootVehicle", 10)) {
            CompoundTag compoundtag = optional1.get().getCompound("RootVehicle");
            Entity entity = EntityType.loadEntityRecursive(
                    compoundtag.getCompound("Entity"), serverlevel1, p_215603_ -> !serverlevel1.addWithUUID(p_215603_) ? null : p_215603_
            );
            if (entity != null) {
                UUID uuid;
                if (compoundtag.hasUUID("Attach")) {
                    uuid = compoundtag.getUUID("Attach");
                } else {
                    uuid = null;
                }

                if (entity.getUUID().equals(uuid)) {
                    pPlayer.startRiding(entity, true);
                } else {
                    for (Entity entity1 : entity.getIndirectPassengers()) {
                        if (entity1.getUUID().equals(uuid)) {
                            pPlayer.startRiding(entity1, true);
                            break;
                        }
                    }
                }

                if (!pPlayer.isPassenger()) {
                    DistributedServerLevels.LOGGER.warn("Couldn't reattach entity to player");
                    entity.discard();

                    for (Entity entity2 : entity.getIndirectPassengers()) {
                        entity2.discard();
                    }
                }
            }
        }

        pPlayer.initInventoryMenu();
        net.neoforged.neoforge.event.EventHooks.firePlayerLoggedIn( pPlayer );
    }
}
