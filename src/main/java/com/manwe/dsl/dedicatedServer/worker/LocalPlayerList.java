package com.manwe.dsl.dedicatedServer.worker;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerGamePacketListenerImpl;
import com.manwe.dsl.mixin.accessors.PlayerListAccessor;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.PlayerDataStorage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class LocalPlayerList extends DedicatedPlayerList {
    public LocalPlayerList(DedicatedServer pServer, LayeredRegistryAccess<RegistryLayer> pRegistries, PlayerDataStorage pPlayerIo) {
        super(pServer, pRegistries, pPlayerIo);
    }

    @Override
    public void placeNewPlayer(Connection pConnection, ServerPlayer pPlayer, CommonListenerCookie pCookie) {
        GameProfile gameprofile = pPlayer.getGameProfile();
        GameProfileCache gameprofilecache = this.getServer().getProfileCache();
        if (gameprofilecache != null) gameprofilecache.add(gameprofile);

         //TODO Hay que ver esto de las dimensiones
        Optional<CompoundTag> optional1 = this.load(pPlayer);
        ResourceKey<Level> resourcekey = optional1.<ResourceKey<Level>>flatMap(
            p_337568_ -> DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, p_337568_.get("Dimension"))).resultOrPartial(DistributedServerLevels.LOGGER::error)
        ).orElse(Level.OVERWORLD);
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
                "Worker: {}[{}] logged in with entity id {} at ({}, {}, {})",
                pPlayer.getName().getString(),
                s1,
                pPlayer.getId(),
                pPlayer.getX(),
                pPlayer.getY(),
                pPlayer.getZ()
        );
        LevelData leveldata = serverlevel1.getLevelData();
        pPlayer.loadGameTypes(optional1.orElse(null));

        /*
        GameRules gamerules = serverlevel1.getGameRules();
        boolean flag = gamerules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN);
        boolean flag1 = gamerules.getBoolean(GameRules.RULE_REDUCEDDEBUGINFO);
        boolean flag2 = gamerules.getBoolean(GameRules.RULE_LIMITED_CRAFTING);
        pConnection.send(
                new ClientboundLoginPacket(
                        pPlayer.getId(),
                        leveldata.isHardcore(),
                        getServer().levelKeys(),
                        this.getMaxPlayers(),
                        this.getViewDistance(),
                        this.getSimulationDistance(),
                        flag1,
                        !flag,
                        flag2,
                        pPlayer.createCommonSpawnInfo(serverlevel1),
                        getServer().enforceSecureProfile()
                )
        );*/ //TODO de momento el ClientboundLoginPacket lo maneja el proxy

        pConnection.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
        pConnection.send(new ClientboundPlayerAbilitiesPacket(pPlayer.getAbilities()));
        pConnection.send(new ClientboundSetCarriedItemPacket(pPlayer.getInventory().selected));

        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.OnDatapackSyncEvent(this, pPlayer));
        pConnection.send(new ClientboundUpdateRecipesPacket(this.getServer().getRecipeManager().getOrderedRecipes()));
        this.sendPlayerPermissionLevel(pPlayer); //TODO Hay que mandar esto?
        pPlayer.getStats().markAllDirty();
        pPlayer.getRecipeBook().sendInitialRecipeBook(pPlayer); //TODO Hay que mandar esta información?
        this.updateEntireScoreboard(serverlevel1.getScoreboard(), pPlayer);
        this.getServer().invalidateStatus();
        if(pConnection.getPacketListener() instanceof WorkerGamePacketListenerImpl serverGamePacketListener){
            serverGamePacketListener.teleport(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), pPlayer.getYRot(), pPlayer.getXRot());
        }
        /*
        ServerStatus serverstatus = this.getServer().getStatus();
        if (serverstatus != null && !pCookie.transferred()) {
            pPlayer.sendServerStatus(serverstatus); //TODO Hay que mandar el server status?
        }*/

        pPlayer.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(this.getPlayers())); //TODO Hay que mandar esta información?
        ((PlayerListAccessor) this).getPlayers().add(pPlayer);
        ((PlayerListAccessor) this).getPlayersByUUID().put(pPlayer.getUUID(), pPlayer);
        this.broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(pPlayer)));
        this.sendLevelInfo(pPlayer, serverlevel1); //TODO Mandar?
        serverlevel1.addNewPlayer(pPlayer);
        this.getServer().getCustomBossEvents().onPlayerConnect(pPlayer);
        this.sendActivePlayerEffects(pPlayer); //TODO Hay que manejar los efectos en el worker?
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

        pPlayer.initInventoryMenu(); //TODO Hay que gestionar el inventario en el worker?
        net.neoforged.neoforge.event.EventHooks.firePlayerLoggedIn( pPlayer );
    }

    public void transferExistingPlayer(ServerPlayer pPlayer, CompoundTag nbt){

        /* ---------- 1. Determinar el mundo destino ---------- */
        ResourceKey<Level> key = DimensionType
                .parseLegacy(new Dynamic<>(NbtOps.INSTANCE, nbt.get("Dimension")))
                .resultOrPartial(DistributedServerLevels.LOGGER::error)
                .orElse(Level.OVERWORLD);

        ServerLevel level = Objects.requireNonNullElseGet(this.getServer().getLevel(key), this.getServer()::overworld); // fallback → overworld

        pPlayer.setServerLevel(level); // vincula la entidad al Level correcto

        ((PlayerListAccessor) this).getPlayers().add(pPlayer);
        ((PlayerListAccessor) this).getPlayersByUUID().put(pPlayer.getUUID(), pPlayer);

        level.addNewPlayer(pPlayer);
        pPlayer.initInventoryMenu(); //TODO Hay que gestionar el inventario en el worker?

        /* ---------- 3. Restaurar montura, si la hubiera ---------- */
        if (nbt.contains("RootVehicle", Tag.TAG_COMPOUND)) {
            CompoundTag rv = nbt.getCompound("RootVehicle");
            Entity mount = EntityType.loadEntityRecursive(rv.getCompound("Entity"), level,
                    e -> level.addWithUUID(e) ? e : null);

            if (mount != null) {
                UUID attach = rv.hasUUID("Attach") ? rv.getUUID("Attach") : null;
                Entity target = mount;                         // por defecto la raíz
                if (attach != null && !mount.getUUID().equals(attach)) {
                    target = null;
                    for (Entity e : mount.getIndirectPassengers()) {  // ← bucle, no stream()
                        if (e.getUUID().equals(attach)) {
                            target = e;
                            break;
                        }
                    }
                }

                if (target != null) pPlayer.startRiding(target, true);
                else {                         // fallo → limpiar restos fantasma
                    mount.discard();
                    mount.getIndirectPassengers().forEach(Entity::discard);
                    DistributedServerLevels.LOGGER.warn("Couldn't reattach mount for {}", pPlayer.getName().getString());
                }
            }
        }
        this.sendPlayerPermissionLevel(pPlayer);// TODO ver esto
    }
}
