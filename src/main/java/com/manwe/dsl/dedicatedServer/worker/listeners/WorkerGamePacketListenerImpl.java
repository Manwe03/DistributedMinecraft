package com.manwe.dsl.dedicatedServer.worker.listeners;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.config.DSLServerConfigs;
import com.manwe.dsl.connectionRouting.RegionRouter;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerInformationPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerLoginPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerMovePacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.transfer.ProxyBoundPlayerTransferPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.chunkloading.WorkerBoundFakePlayerLoginPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerDisconnectPacket;
import com.manwe.dsl.mixin.accessors.ServerGamePacketListenerImplAccessor;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;


public class WorkerGamePacketListenerImpl extends ServerGamePacketListenerImpl {

    private final WorkerListenerImpl workerListener;

    int workerSize = DSLServerConfigs.WORKER_SIZE.get();
    int regionSize = DSLServerConfigs.REGION_SIZE.get();
    int workerId = DSLServerConfigs.WORKER_ID.get();

    ChunkPos oldChunkPos;

    BitSet preloadedWorkers;

    //List of the current fakePlayers of this player in other workers
    //private Set<Integer> preloadedWorkers = new HashSet<>(DSLServerConfigs.WORKER_SIZE.get());

    public WorkerGamePacketListenerImpl(MinecraftServer pServer, Connection pConnection, ServerPlayer pPlayer, CommonListenerCookie pCookie, WorkerListenerImpl workerListener, BitSet preloadedWorkers) {
        super(pServer, pConnection, pPlayer, pCookie);
        this.workerListener = workerListener;
        this.preloadedWorkers = preloadedWorkers;
        this.preloadedWorkers.clear(workerId); //Remove this worker from preloaded

        //testViewOutsideWorkerBounds(); //Fake Player creation in initialization? no funciona muy bien por la posición
    }

    @Override
    public void handlePlayerCommand(ServerboundPlayerCommandPacket pPacket) {
        super.handlePlayerCommand(pPacket);
    }

    @Override
    public void handleClientInformation(ServerboundClientInformationPacket pPacket) {
        super.handleClientInformation(pPacket);
        workerListener.send(new ProxyBoundFakePlayerInformationPacket(player.getUUID(), preloadedWorkers, player.requestedViewDistance()));
    }

    @Override
    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket pPacket) {
        //System.out.println("ACK -> Packet Id:" + pPacket.getId() + " AwaitingTeleport: " + ((ServerGamePacketListenerImplAccessor)this).getAwaitingTeleport() + " AwaitingPos: " + ((ServerGamePacketListenerImplAccessor)this).getAwaitingPositionFromClient());
        super.handleAcceptTeleportPacket(pPacket);
        //System.out.println("Post ACK -> Packet Id:" + pPacket.getId() + " AwaitingTeleport: " + ((ServerGamePacketListenerImplAccessor)this).getAwaitingTeleport() + " AwaitingPos: " + ((ServerGamePacketListenerImplAccessor)this).getAwaitingPositionFromClient());
    }

    @Override
    public void onDisconnect(DisconnectionDetails pDetails) {
        workerListener.send(new ProxyBoundFakePlayerDisconnectPacket(player.getUUID(),preloadedWorkers)); //Send Disconnect to all fake players, they will execute WorkerFakePlayerListenerImpl.onDisconnect
        super.onDisconnect(pDetails); //Disconnect this player
    }

    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket pPacket) {
        oldChunkPos = player.chunkPosition();
        super.handleMovePlayer(pPacket);
        if(!oldChunkPos.equals(player.chunkPosition())){ //Player changed chunk
            System.out.println("Player changed chunks");
            testOutsideWorkerBounds();      //Player Transfer
            testViewOutsideWorkerBounds();  //Fake Player creation
            sendFakePlayerMovement();       //Fake Player movement synchronization
        }
    }

    /**
     * Handle send transfer request to proxy and removes this connection
     */
    private void testOutsideWorkerBounds() {
        int id = RegionRouter.computeWorkerId(player.getX(),player.getZ(), workerSize, regionSize);
        if(id != DSLServerConfigs.WORKER_ID.get()){ //Transfer
            workerListener.setTransfering(player.getUUID());
            workerListener.send(new ProxyBoundPlayerTransferPacket(player, id, preloadedWorkers));
            DistributedServerLevels.LOGGER.info("Transfer in progress block all incoming packets from ["+ player.getUUID()+"] to this worker");
        }
    }

    /**
     * Handle fake player login if other worker is in view distance
     */
    private void testViewOutsideWorkerBounds() {
        System.out.println("Test view outside bounds");

        int blockViewDistance = Mth.clamp(player.requestedViewDistance(), 2, server.getPlayerList().getViewDistance()) << 4;
        int px = Mth.floor(player.getX());
        int pz = Mth.floor(player.getZ());

        BitSet newPreloaded = new BitSet(); //Set the bits to 1 of the visible workers
        newPreloaded.set(RegionRouter.computeWorkerId(px + blockViewDistance, pz + blockViewDistance, workerSize, regionSize));
        newPreloaded.set(RegionRouter.computeWorkerId(px - blockViewDistance, pz + blockViewDistance, workerSize, regionSize));
        newPreloaded.set(RegionRouter.computeWorkerId(px + blockViewDistance, pz - blockViewDistance, workerSize, regionSize));
        newPreloaded.set(RegionRouter.computeWorkerId(px - blockViewDistance, pz - blockViewDistance, workerSize, regionSize));

        newPreloaded.clear(workerId);       //Remove this worker

        //XOR
        BitSet diff = new BitSet();
        diff.or(newPreloaded);
        diff.xor(preloadedWorkers);
        //To Add
        BitSet add = new BitSet();
        add.or(diff);
        add.and(newPreloaded);
        //To Remove
        BitSet remove = new BitSet();
        remove.or(diff);
        remove.and(preloadedWorkers);

        if(add.cardinality() > 0) {
            System.out.println("ProxyBoundFakePlayerLoginPacket Pos:"+player.position());
            workerListener.send(new ProxyBoundFakePlayerLoginPacket(player, add));
        }
        if(remove.cardinality() > 0){
            System.out.println("ProxyBoundFakePlayerDisconnectPacket - "+ remove);
            workerListener.send(new ProxyBoundFakePlayerDisconnectPacket(player.getUUID(), remove));
        }

        preloadedWorkers = newPreloaded;    //Update bitset
    }

    /**
     * Sends to the proxy the new location of the fake players in other workers
     */
    private void sendFakePlayerMovement(){
        workerListener.send(new ProxyBoundFakePlayerMovePacket(player.getUUID(), player.position(), preloadedWorkers)); //Send set position of fake players
    }

    @Override
    public void handleKeepAlive(ServerboundKeepAlivePacket pPacket) {
        super.handleKeepAlive(pPacket);
        System.out.println("(NO debería) keep alive recibido en el worker");
    }

    @Override
    protected void keepConnectionAlive() {
        //Desactiva el keep alive del worker, esto lo maneja solo el proxy
    }
}
