package com.manwe.dsl.dedicatedServer.worker;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.config.DSLServerConfigs;
import com.manwe.dsl.connectionRouting.RegionRouter;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.transfer.ProxyBoundEntityTransferPacket;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerGamePacketListenerImpl;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListenerImpl;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

import java.util.NoSuchElementException;

public class WorkerBoundaryListener {

    @SubscribeEvent
    public void onEnteringSection(EntityEvent.EnteringSection event){
        if (event.didChunkChange()) {
            int chunkX = SectionPos.x(event.getPackedNewPos());
            int chunkZ = SectionPos.z(event.getPackedNewPos());
            int id = RegionRouter.computeWorkerIdChunk(chunkX, chunkZ);
            checkAndTransfer(event.getEntity(),id);
        }
    }

    @SubscribeEvent
    public void onEntityTeleportation(EntityTeleportEvent e){
        int id = RegionRouter.computeWorkerId(e.getTargetX(), e.getTargetZ());
        checkAndTransfer(e.getEntity(),id);
    }

    private static void checkAndTransfer(Entity entity, int id) {
        if (entity.level().isClientSide()) return;
        if (entity.isRemoved()) return;

        try {
            if(!(entity.getServer().getConnection().getConnections().getFirst() instanceof WorkerConnection workerConnection &&
                workerConnection.getPacketListener() instanceof WorkerListenerImpl workerListener)){
                return;
            }

            if(id != DSLServerConfigs.WORKER_ID.get()){
                if(entity instanceof ServerPlayer player){
                    if(player.connection instanceof WorkerGamePacketListenerImpl packetListener){
                        packetListener.transferPlayer();
                    }
                }else {
                    //System.out.println("Transfer Entity NeoForge");
                    workerListener.send(new ProxyBoundEntityTransferPacket(entity,id));
                    try {
                        entity.remove(Entity.RemovalReason.DISCARDED); //DELETE
                    } catch (ArrayIndexOutOfBoundsException e){
                        DistributedServerLevels.LOGGER.error("Entity could not be removed from source worker: "+entity.getName().getString(),e);
                    }
                }
            }
        } catch (NoSuchElementException exception){
            DistributedServerLevels.LOGGER.info("Entity tried to transfer before proxy initialization");
            //If some entity tried to change worker before proxy initialization this exception is thrown and we should prevent movement
        }
    }
}
