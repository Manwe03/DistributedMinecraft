package com.manwe.dsl.dedicatedServer.worker;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.dedicatedServer.CustomDedicatedServer;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

public class MetricFileLogger {
    private static final int FLUSH_EVERY_TICKS = 100;
    private static final Map<Integer, BufferedWriter> MSPT_WRITERS = new HashMap<>();
    private static final Map<Integer, BufferedWriter> MEM_WRITERS = new HashMap<>();
    private static final Map<Integer, Integer> MSPT_CURSOR = new HashMap<>();
    private static final Map<Integer, Integer> MEM_CURSOR = new HashMap<>();

    private static Path metricsDir;
    private static int  tickCounter;

    public static void init(MinecraftServer srv) throws IOException {
        metricsDir = srv.getFile("metrics");
        Files.createDirectories(metricsDir);
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent e) {
        try {
            MetricFileLogger.init(e.getServer());
        } catch (IOException ex) {
            DistributedServerLevels.LOGGER.error("No se pudo iniciar el logger de métricas", ex);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e) {
        if (!(e.getServer() instanceof CustomDedicatedServer server)) return;
        if (++tickCounter % FLUSH_EVERY_TICKS != 0) return;

        System.out.println("Tick Log mspt");

        server.workersNanoTicks.forEach((id, list) -> {
            int         start = MSPT_CURSOR.getOrDefault(id, 0);

            if (start < list.size()) {

                BufferedWriter w = MSPT_WRITERS.get(id);
                if(w == null) {
                    w = MetricFileLogger.openWriter(id,"mspt");
                    MSPT_WRITERS.put(id,w);
                }

                // escribe todas las muestras pendientes
                for (int i = start; i < list.size(); i++) {
                    double mspt = list.get(i) / 1_000_000.0;    // ns → ms
                    try {
                        w.write(String.format(Locale.US, "%.5f", mspt));
                        w.newLine();
                    } catch (IOException ex) {
                        LogUtils.getLogger().error("No puedo escribir métricas [W" + id + "]", ex);
                    }
                }
                MSPT_CURSOR.put(id, list.size()); // avanza cursor
                try {
                    w.flush();
                } catch (IOException ignored) {
                }
            }
        });

        server.workersMem.forEach((id, list) -> {
            int start = MEM_CURSOR.getOrDefault(id, 0);
            if (start < list.size()) {

                BufferedWriter w = MEM_WRITERS.get(id);
                if(w == null) {
                    w = MetricFileLogger.openWriter(id,"mem");
                    MEM_WRITERS.put(id,w);
                }

                // escribe todas las muestras pendientes
                for (int i = start; i < list.size(); i++) {
                    try {
                        //System.out.println("tps: "+list.get(i));
                        w.write(""+list.get(i));
                        w.newLine();
                    } catch (IOException ex) {
                        LogUtils.getLogger().error("No puedo escribir métricas [W" + id + "]", ex);
                    }
                }
                MEM_CURSOR.put(id, list.size()); // avanza cursor
                try {
                    w.flush();
                } catch (IOException ignored) {
                }
            }
        });
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent e) {
        MSPT_WRITERS.values().forEach(w -> {                     // cierra todos
            try { w.close(); } catch (IOException ignored) {}
        });
        MSPT_WRITERS.clear();

        MEM_WRITERS.values().forEach(w -> {                     // cierra todos
            try { w.close(); } catch (IOException ignored) {}
        });
        MEM_WRITERS.clear();
    }

    private static BufferedWriter openWriter(int workerId, String prefx) {
        try {
            String fileName = "worker-" + workerId + "-"+prefx+"-" + LocalDate.now(ZoneOffset.UTC) + ".csv";
            Path   file = metricsDir.resolve(fileName);

            boolean newFile = Files.notExists(file);
            BufferedWriter w = Files.newBufferedWriter(
                    file, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            if (newFile) w.write(prefx+"\n");     // cabecera
            return w;
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo abrir CSV de W" + workerId, ex);
        }
    }
}
