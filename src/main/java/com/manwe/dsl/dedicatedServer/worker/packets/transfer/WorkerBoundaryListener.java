package com.manwe.dsl.dedicatedServer.worker.packets.transfer;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.config.DSLServerConfigs;
import com.manwe.dsl.connectionRouting.RegionRouter;
import com.manwe.dsl.dedicatedServer.proxy.ProxyServerConnectionListener;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.transfer.ProxyBoundEntityTransferPacket;
import com.manwe.dsl.dedicatedServer.worker.WorkerConnection;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListenerImpl;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;

import java.util.NoSuchElementException;

public class WorkerBoundaryListener {

    @SubscribeEvent
    public void onEnteringSection(EntityEvent.EnteringSection event){

        Entity entity = event.getEntity();
        if (entity.level().isClientSide()) return;   // Solo servidor
        if (entity.isRemoved())            return;   // Ya eliminada
        if (!event.didChunkChange()) return;

        // 3. Traducir sección → coordenadas de bloque (~centro del chunk)
        int chunkX = SectionPos.x(event.getPackedNewPos());
        int chunkZ = SectionPos.z(event.getPackedNewPos());
        int blockX = (chunkX << 4) + 8;
        int blockZ = (chunkZ << 4) + 8;

        int id = RegionRouter.computeWorkerId(blockX, blockZ);

        try {

            if(!(entity.getServer().getConnection().getConnections().getFirst() instanceof WorkerConnection workerConnection && workerConnection.getPacketListener() instanceof WorkerListenerImpl workerListener)){
                return;
            }

            if(id != DSLServerConfigs.WORKER_ID.get()){
                if(entity instanceof Player){
                    System.out.println("Transfer Player NeoForge");
                    //Ignored
                }else {
                    System.out.println("Transfer Entity NeoForge");
                    workerListener.send(new ProxyBoundEntityTransferPacket(entity,id));
                }
            }
        } catch (NoSuchElementException exception){
            DistributedServerLevels.LOGGER.info("Entity tried to transfer before proxy initialization");
            //If some entity tried to change worker before proxy initialization this exception is thrown and we should prevent movement
            SectionPos oldPos = event.getOldPos();
            entity.setPosRaw(oldPos.x(),oldPos.y(),oldPos.z());
        }
    }
}
