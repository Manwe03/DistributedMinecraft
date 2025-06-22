package com.manwe.dsl.dedicatedServer.worker;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.dedicatedServer.CustomDedicatedServer;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.ProxyBoundHealthPacket;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListenerImpl;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class LocalProfiler {

    private static final int SAMPLE_SIZE = 100;

    private static final long[] history = new long[SAMPLE_SIZE];
    private static final List<Integer> history_tps = new ArrayList<>();
    private static final List<Long> history_mem = new ArrayList<>();
    private static int index = 0;

    private static long tickStartNanos;

    private long lastTPSnano = 0;
    //private int ticksThisSecond = 0;

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
                //int[] tpsArray = history_tps.stream().mapToInt(i->i).toArray();
                long[] memArray = history_mem.stream().mapToLong(i->i).toArray();
                workerListener.send(new ProxyBoundHealthPacket(history, memArray, customDedicatedServer.workerId));
            } catch (NoSuchElementException exception){
                //Ignore
                DistributedServerLevels.LOGGER.info("Tried to send mspt info, Proxy not yet connected");
            }
            index = 0;
        }
        //Save MSPT
        history[index] = duration;

        //Save TPS
        if(System.nanoTime() - lastTPSnano > 1_000_000_000){
            history_mem.add(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() / 1024L / 1024L);
            lastTPSnano = System.nanoTime();
        }
    }
}
