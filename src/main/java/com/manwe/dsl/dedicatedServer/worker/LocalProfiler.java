package com.manwe.dsl.dedicatedServer.worker;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.dedicatedServer.CustomDedicatedServer;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.ProxyBoundHealthPacket;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListenerImpl;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.NoSuchElementException;

public class LocalProfiler {

    private static final int SAMPLE_SIZE = 100;

    private static final long[] history = new long[SAMPLE_SIZE];
    private static int index = 0;

    private static long tickStartNanos;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Pre e) {
        if(e.getServer() instanceof CustomDedicatedServer customDedicatedServer && customDedicatedServer.isProxy()) return;
        tickStartNanos = System.nanoTime();
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post e) {
        if(e.getServer() instanceof CustomDedicatedServer customDedicatedServer && customDedicatedServer.isProxy()) return;
        long duration = System.nanoTime() - tickStartNanos; // ns que tardó el tick
        if(++index == SAMPLE_SIZE){
            try {
                if(!(e.getServer() instanceof CustomDedicatedServer customDedicatedServer && customDedicatedServer.getConnection().getConnections().getFirst() instanceof WorkerConnection workerConnection && workerConnection.getPacketListener() instanceof WorkerListenerImpl workerListener)){
                    return;
                }
                workerListener.send(new ProxyBoundHealthPacket(history, customDedicatedServer.workerId));
            } catch (NoSuchElementException exception){
                //Ignore
                DistributedServerLevels.LOGGER.info("Tried to send mspt info, Proxy not yet connected");
            }
            index = 0;
        }
        history[index] = duration;
    }
}
