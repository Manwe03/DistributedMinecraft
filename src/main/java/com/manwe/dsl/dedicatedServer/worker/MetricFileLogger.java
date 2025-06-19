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
    private static final Map<Integer, BufferedWriter> WRITERS = new HashMap<>();
    private static final Map<Integer, Integer>        CURSOR  = new HashMap<>();

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

        long now = System.currentTimeMillis();

        server.workersNanoTicks.forEach((integer, longs) -> {
            int         id    = integer;
            List<Long>  list  = longs;
            int         start = CURSOR.getOrDefault(id, 0);

            System.out.println("Write log "+ id);

            if (start < list.size()) {

                BufferedWriter w = WRITERS.computeIfAbsent(id, MetricFileLogger::openWriter);

                // escribe todas las muestras pendientes
                for (int i = start; i < list.size(); i++) {
                    double mspt = list.get(i) / 1_000_000.0;    // ns → ms
                    try {
                        w.write(now + "," + String.format(Locale.US, "%.5f", mspt));
                        w.newLine();
                    } catch (IOException ex) {
                        LogUtils.getLogger().error("No puedo escribir métricas W" + id, ex);
                    }
                }
                CURSOR.put(id, list.size());                   // avanza cursor
                try {
                    w.flush();
                } catch (IOException ignored) {
                }
            }
        });
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent e) {
        WRITERS.values().forEach(w -> {                     // cierra todos
            try { w.close(); } catch (IOException ignored) {}
        });
        WRITERS.clear();
    }

    private static BufferedWriter openWriter(int workerId) {
        try {
            String fileName = "worker-" + workerId + "-"
                    + LocalDate.now(ZoneOffset.UTC) + ".csv";
            Path   file = metricsDir.resolve(fileName);

            boolean newFile = Files.notExists(file);
            BufferedWriter w = Files.newBufferedWriter(
                    file, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            if (newFile) w.write("epochMillis,mspt\n");     // cabecera
            return w;
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo abrir CSV de W" + workerId, ex);
        }
    }
}
