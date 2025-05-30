package com.manwe.dsl.dedicatedServer.proxy;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.config.DSLServerConfigs;
import com.manwe.dsl.connectionRouting.RegionRouter;
import com.manwe.dsl.dedicatedServer.proxy.front.listeners.ProxyServerGameListener;
import com.manwe.dsl.dedicatedServer.worker.packets.WorkerBoundPlayerInitPacket;
import com.manwe.dsl.mixin.accessors.PlayerListAccessor;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import net.minecraft.ChatFormatting;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.PlayerDataStorage;

import java.util.*;

/**
 * Modified PlaceNewPlayer, set up RemoteServerGamePacketListenerImpl instead of ServerGamePacketListenerImpl
 */
public class RemotePlayerList extends DedicatedPlayerList {

    RegionRouter router; //Un router por proxy es decir solo 1

    public RemotePlayerList(DedicatedServer pServer, LayeredRegistryAccess<RegistryLayer> pRegistries, PlayerDataStorage pPlayerIo) {
        super(pServer, pRegistries, pPlayerIo);
        if(!(((PlayerListAccessor) this).getServer() instanceof ProxyDedicatedServer proxyDedicatedServer)) throw new RuntimeException("placeNewPlayer from RemotePlayerList was not called in a ProxyDedicatedServer");
        //Create router
        this.router = new RegionRouter(proxyDedicatedServer);
    }

    @Override
    public void placeNewPlayer(Connection pConnection, ServerPlayer pPlayer, CommonListenerCookie pCookie) {
        if(!(((PlayerListAccessor) this).getServer() instanceof ProxyDedicatedServer proxyDedicatedServer)) throw new RuntimeException("placeNewPlayer from RemotePlayerList was not called in a ProxyDedicatedServer");
        GameProfile gameprofile = pPlayer.getGameProfile();
        GameProfileCache gameprofilecache = proxyDedicatedServer.getProfileCache();
        String s;
        if (gameprofilecache != null) {
            Optional<GameProfile> optional = gameprofilecache.get(gameprofile.getId());
            s = optional.map(GameProfile::getName).orElse(gameprofile.getName());
            gameprofilecache.add(gameprofile);
        } else {
            s = gameprofile.getName();
        }

        Optional<CompoundTag> optional1 = this.load(pPlayer);
        ResourceKey<Level> resourcekey = optional1.<ResourceKey<Level>>flatMap(
                        p_337568_ -> DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, p_337568_.get("Dimension"))).resultOrPartial(DistributedServerLevels.LOGGER::error)
                )
                .orElse(Level.OVERWORLD);
        ServerLevel serverlevel = proxyDedicatedServer.getLevel(resourcekey);
        ServerLevel serverlevel1;
        if (serverlevel == null) {
            DistributedServerLevels.LOGGER.warn("Unknown respawn dimension {}, defaulting to overworld", resourcekey);
            serverlevel1 = proxyDedicatedServer.overworld();
        } else {
            serverlevel1 = serverlevel;
        }
        /*
        pPlayer.setServerLevel(serverlevel1);
        String s1 = pConnection.getLoggableAddress(proxyDedicatedServer.logIPs());
        DistributedServerLevels.LOGGER.info(
                "{}[{}] logged in with entity id {} at ({}, {}, {})",
                pPlayer.getName().getString(),
                s1,
                pPlayer.getId(),
                pPlayer.getX(),
                pPlayer.getY(),
                pPlayer.getZ()
        );
        */
        LevelData leveldata = serverlevel1.getLevelData();
        //pPlayer.loadGameTypes(optional1.orElse(null));

        if(!proxyDedicatedServer.isProxy())throw new RuntimeException("Worker cannot have a remotePlayerList");

        //Register this Client<->Proxy connection to be used by outgoing client packets from workers to clients. The proxy redirects these packets to the client.
        this.router.addOutgoingConnection(pPlayer.getUUID(),pConnection);
        //Create init message
        WorkerBoundPlayerInitPacket initPacket = new WorkerBoundPlayerInitPacket(pPlayer.getGameProfile(), pPlayer.clientInformation());

        //TODO guardar la posición de los jugadores al desconectarse para enrutarlos con el servidor correcto en caso de reinicio
        //If this player has no worker set
        if(!this.router.hasTunnel(pPlayer.getUUID())){
            //Set tunnel worker in default spawn position
            System.out.println("This player has no tunnel set defaulting to server spawn");
            this.router.transferClientToWorker(pPlayer.getUUID(), RegionRouter.defaultSpawnWorkerId(getServer(), DSLServerConfigs.WORKER_SIZE.get(),DSLServerConfigs.REGION_SIZE.get()));
        }
        this.router.route(pPlayer.getUUID()).send(initPacket); //Send init to worker

        DistributedServerLevels.LOGGER.info("Broadcast player login to all workers");

        ProxyServerGameListener servergamepacketlistenerimpl = new ProxyServerGameListener(proxyDedicatedServer, pConnection, pPlayer, pCookie, this.router);
        System.out.println("Proxy: ProxyServerGameListener");

        pConnection.setupInboundProtocol(
                GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(proxyDedicatedServer.registryAccess(), servergamepacketlistenerimpl.getConnectionType())), servergamepacketlistenerimpl
        );

        GameRules gamerules = serverlevel1.getGameRules();
        boolean flag = gamerules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN);
        boolean flag1 = gamerules.getBoolean(GameRules.RULE_REDUCEDDEBUGINFO);
        boolean flag2 = gamerules.getBoolean(GameRules.RULE_LIMITED_CRAFTING);
        servergamepacketlistenerimpl.send(
                new ClientboundLoginPacket(
                        pPlayer.getId(),
                        leveldata.isHardcore(),
                        proxyDedicatedServer.levelKeys(),
                        this.getMaxPlayers(),
                        this.getViewDistance(),
                        this.getSimulationDistance(),
                        flag1,
                        !flag,
                        flag2,
                        pPlayer.createCommonSpawnInfo(serverlevel1),
                        proxyDedicatedServer.enforceSecureProfile()
                )
        );

        //servergamepacketlistenerimpl.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
        //servergamepacketlistenerimpl.send(new ClientboundPlayerAbilitiesPacket(pPlayer.getAbilities()));
        //servergamepacketlistenerimpl.send(new ClientboundSetCarriedItemPacket(pPlayer.getInventory().selected));

        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.OnDatapackSyncEvent(this, pPlayer));
        //servergamepacketlistenerimpl.send(new ClientboundUpdateRecipesPacket(proxyDedicatedServer.getRecipeManager().getOrderedRecipes()));
        //this.sendPlayerPermissionLevel(pPlayer);
        pPlayer.getStats().markAllDirty();
        //pPlayer.getRecipeBook().sendInitialRecipeBook(pPlayer);
        //this.updateEntireScoreboard(serverlevel1.getScoreboard(), pPlayer);
        proxyDedicatedServer.invalidateStatus();
        MutableComponent mutablecomponent;
        if (pPlayer.getGameProfile().getName().equalsIgnoreCase(s)) {
            mutablecomponent = Component.translatable("multiplayer.player.joined", pPlayer.getDisplayName());
        } else {
            mutablecomponent = Component.translatable("multiplayer.player.joined.renamed", pPlayer.getDisplayName(), s);
        }

        this.broadcastSystemMessage(mutablecomponent.withStyle(ChatFormatting.YELLOW), false);
        //servergamepacketlistenerimpl.teleport(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), pPlayer.getYRot(), pPlayer.getXRot());
        ServerStatus serverstatus = proxyDedicatedServer.getStatus();
        if (serverstatus != null && !pCookie.transferred()) {
            pPlayer.sendServerStatus(serverstatus);
        }

        //pPlayer.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(((PlayerListAccessor) this).getPlayers()));
        //((PlayerListAccessor) this).getPlayers().add(pPlayer);
        //((PlayerListAccessor) this).getPlayersByUUID().put(pPlayer.getUUID(), pPlayer);
        //this.broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(pPlayer)));
        //this.sendLevelInfo(pPlayer, serverlevel1);
        //serverlevel1.addNewPlayer(pPlayer);
        //proxyDedicatedServer.getCustomBossEvents().onPlayerConnect(pPlayer);
        //this.sendActivePlayerEffects(pPlayer);
        /*
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
        */
    }

}
