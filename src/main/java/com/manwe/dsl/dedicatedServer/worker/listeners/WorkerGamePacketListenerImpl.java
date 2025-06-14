package com.manwe.dsl.dedicatedServer.worker.listeners;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.config.DSLServerConfigs;
import com.manwe.dsl.connectionRouting.RegionRouter;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerInformationPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerLoginPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerMovePacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.transfer.ProxyBoundPlayerTransferPacket;
import com.manwe.dsl.dedicatedServer.worker.LocalPlayerList;
import com.manwe.dsl.mixin.accessors.ServerGamePacketListenerImplAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
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
    public void onDisconnect(DisconnectionDetails pDetails) {
        System.out.println("OnDisconnect called in WorkerGamePacketListenerImpl sending request disconnect to all fake players");
        workerListener.send(new ProxyBoundFakePlayerDisconnectPacket(player.getUUID(),preloadedWorkers)); //Send Disconnect to all fake players, they will execute WorkerFakePlayerListenerImpl.onDisconnect
        super.onDisconnect(pDetails); //Disconnect this player
    }

    public void silentDisconnect(){
        ((ServerGamePacketListenerImplAccessor)this).getChatMessageChain().close();
        this.server.invalidateStatus();
        this.server.getPlayerList().broadcastSystemMessage(Component.translatable("multiplayer.player.left", this.player.getDisplayName()).withStyle(ChatFormatting.AQUA), false);
        this.player.disconnect();
        if(!(this.server.getPlayerList() instanceof LocalPlayerList localPlayerList)) throw new RuntimeException("playerList not instance of LocalPlayerList");
        localPlayerList.silentRemovePlayer(this.player);
        //this.server.getPlayerList().remove(this.player);
        this.player.getTextFilter().leave();
    }

    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket pPacket) {
        ChunkPos currentChunk = player.chunkPosition();

        double nextX = clampHorizontal(pPacket.getX(this.player.getX()));
        double nextY = clampVertical(pPacket.getY(this.player.getY()));
        double nextZ = clampHorizontal(pPacket.getZ(this.player.getZ()));
        float fY = Mth.wrapDegrees(pPacket.getYRot(this.player.getYRot()));
        float fX = Mth.wrapDegrees(pPacket.getXRot(this.player.getXRot()));

        //ChunkPos nextChunk = new ChunkPos(Mth.floor(nextX) >> 4, Mth.floor(nextZ) >> 4);
        boolean chunkChanged = currentChunk.x != Mth.floor(nextX) >> 4 || currentChunk.z != Mth.floor(nextZ) >> 4;
        if(chunkChanged){
            System.out.println("Player changed chunks");
            if(testOutsideWorkerBounds(nextX, nextY, nextZ, fY, fX)){
                testViewOutsideWorkerBounds();  //Fake Player creation
                return;
            }else {
                testViewOutsideWorkerBounds();  //Fake Player creation
                sendFakePlayerMovement();       //Fake Player movement synchronization
            }
        }
        super.handleMovePlayer(pPacket);
    }

    public void updateFakePlayers(){
        System.out.println("updateFakePlayers");
        testViewOutsideWorkerBounds();
        sendFakePlayerMovement();
    }

    /**
     * Handle send transfer request to proxy and removes this connection
     */
    private boolean testOutsideWorkerBounds(double nextX, double nextY, double nextZ, float fY, float fX) {
        int id = RegionRouter.computeWorkerId(nextX,nextZ);
        if(id != DSLServerConfigs.WORKER_ID.get()){ //Transfer
            player.absMoveTo(nextX,nextY,nextZ,fY,fX);     //Possible malicious client packets, no checks
            workerListener.setTransfering(player.getUUID());
            workerListener.send(new ProxyBoundPlayerTransferPacket(player, id, preloadedWorkers));
            DistributedServerLevels.LOGGER.info("Transfer in progress block all incoming packets from ["+ player.getUUID()+"] to this worker");
            return true;
        }
        return false;
    }

    private static double clampHorizontal(double pValue) {
        return Mth.clamp(pValue, -3.0E7, 3.0E7);
    }

    private static double clampVertical(double pValue) {
        return Mth.clamp(pValue, -2.0E7, 2.0E7);
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
        newPreloaded.set(RegionRouter.computeWorkerId(px + blockViewDistance, pz + blockViewDistance));
        newPreloaded.set(RegionRouter.computeWorkerId(px - blockViewDistance, pz + blockViewDistance));
        newPreloaded.set(RegionRouter.computeWorkerId(px + blockViewDistance, pz - blockViewDistance));
        newPreloaded.set(RegionRouter.computeWorkerId(px - blockViewDistance, pz - blockViewDistance));

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
