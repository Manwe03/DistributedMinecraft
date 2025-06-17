package com.manwe.dsl.dedicatedServer.proxy;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.connectionRouting.RegionRouter;
import com.manwe.dsl.dedicatedServer.CustomDedicatedServer;
import com.manwe.dsl.dedicatedServer.proxy.front.listeners.ProxyServerGameListener;
import com.manwe.dsl.dedicatedServer.worker.packets.login.WorkerBoundPlayerLoginPacket;
import com.manwe.dsl.mixin.accessors.PlayerListAccessor;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Modified PlaceNewPlayer, set up RemoteServerGamePacketListenerImpl instead of ServerGamePacketListenerImpl
 */
public class RemotePlayerList extends DedicatedPlayerList {

    private final RegionRouter router; //Un router por proxy es decir solo 1

    public RemotePlayerList(DedicatedServer pServer, LayeredRegistryAccess<RegistryLayer> pRegistries, PlayerDataStorage pPlayerIo, RegionRouter router) {
        super(pServer, pRegistries, pPlayerIo);
        //if(!(((PlayerListAccessor) this).getServer() instanceof CustomDedicatedServer customDedicatedServer)) throw new RuntimeException("placeNewPlayer from RemotePlayerList was not called in a CustomDedicatedServer");
        //Create router
        this.router = router; //new RegionRouter(customDedicatedServer);
    }

    @Override
    public void placeNewPlayer(@NotNull Connection pConnection, @NotNull ServerPlayer pPlayer, @NotNull CommonListenerCookie pCookie) {
        if(!(((PlayerListAccessor) this).getServer() instanceof CustomDedicatedServer customDedicatedServer)) throw new RuntimeException("placeNewPlayer from RemotePlayerList was not called in a CustomDedicatedServer");
        if(!customDedicatedServer.isProxy()) throw new RuntimeException("Worker cannot have a remotePlayerList");

        //LOAD CUSTOM SAVE STATE IN PROXY
        ResourceKey<Level> dim = null;
        int workerId = 0;
        Vec3 position = null;

        Optional<CompoundTag> nbtOpt = this.load(pPlayer);
        if (nbtOpt.isPresent()) {
            CompoundTag nbt = nbtOpt.get();

            /* dimensión ----------------------------------------------------- */
            dim = DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, nbt.get("Dimension")))
                    .resultOrPartial(DistributedServerLevels.LOGGER::error)
                    .orElse(Level.OVERWORLD);

            /* posición ------------------------------------------------------ */
            ListTag pos = nbt.getList("Pos", Tag.TAG_DOUBLE);
            if (pos.size() == 3) {
                double x = pos.getDouble(0);
                double y = pos.getDouble(1);
                double z = pos.getDouble(2);
                position = new Vec3(x,y,z);
            }

            /* worker -------------------------------------------------------- */
            if (nbt.contains("WorkerId", Tag.TAG_INT)) {
                workerId = nbt.getInt("WorkerId");
            }
        }
        //CUSTOM STATE LOADED

        ServerLevel serverlevel1 = dim != null ? customDedicatedServer.getLevel(dim) : customDedicatedServer.overworld();
        if(serverlevel1 == null) throw new RuntimeException("RemotePlayerList tried to get a null ServerLevel");
        //LevelData leveldata = serverlevel1.getLevelData();
        //pPlayer.loadGameTypes(optional1.orElse(null));

        pPlayer.setServerLevel(serverlevel1);
        if(position != null) {
            pPlayer.setPos(position);
            System.out.println("Loaded old position at "+ position);
        }

        //Register this Client<->Proxy connection to be used by outgoing client packets from workers to clients. The proxy redirects these packets to the client.
        this.router.addOutgoingConnection(pPlayer.getUUID(),pConnection);
        //Create init message
        WorkerBoundPlayerLoginPacket initPacket = new WorkerBoundPlayerLoginPacket(pPlayer.getGameProfile(), pPlayer.clientInformation());

        if(workerId == 0){ //Default spawn position
            workerId = router.defaultSpawnWorkerId();
            System.out.println("This player has no tunnel set defaulting to server spawn - Worker: "+workerId);
        }
        this.router.transferClientToWorker(pPlayer.getUUID(), workerId);

        System.out.println("Has ["+pPlayer.getUUID()+"] tunnel "+this.router.hasTunnel(pPlayer.getUUID()));

        ProxyServerGameListener servergamepacketlistenerimpl = new ProxyServerGameListener(customDedicatedServer, pConnection, pPlayer, pCookie, this.router);

        pConnection.setupInboundProtocol(
                GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(customDedicatedServer.registryAccess(), servergamepacketlistenerimpl.getConnectionType())), servergamepacketlistenerimpl
        );

        //net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.OnDatapackSyncEvent(this, pPlayer));
        pPlayer.getStats().markAllDirty();
        customDedicatedServer.invalidateStatus();


        this.router.route(pPlayer.getUUID()).send(initPacket); //Send init to worker
    }

    @Override
    public void tick() {
        super.tick();
    }

    /*
    @Override
    public void broadcastChatMessage(PlayerChatMessage pMessage, ServerPlayer pSender, ChatType.Bound pBoundChatType) {
        this.broadcastChatMessage(pMessage, pSender::shouldFilterMessageTo, pSender, pBoundChatType);
    }

    private void broadcastChatMessage(PlayerChatMessage pMessage, Predicate<ServerPlayer> pShouldFilterMessageTo, @Nullable ServerPlayer pSender, ChatType.Bound pBoundChatType) {
        boolean flag = true;//pMessage.hasSignature() && !pMessage.hasExpiredServer(Instant.now());
        this.getServer().logChatMessage(pMessage.decoratedContent(), pBoundChatType, flag ? null : "Not Secure");
        OutgoingChatMessage outgoingchatmessage = OutgoingChatMessage.create(pMessage);
        boolean flag1 = false;

        for (ServerPlayer serverplayer : this.getPlayers()) {
            boolean flag2 = pShouldFilterMessageTo.test(serverplayer);
            serverplayer.sendChatMessage(outgoingchatmessage, flag2, pBoundChatType);
            flag1 |= flag2 && pMessage.isFullyFiltered();
        }

        if (flag1 && pSender != null) {
            pSender.sendSystemMessage(CHAT_FILTERED_FULL);
        }
    }
     */
}
