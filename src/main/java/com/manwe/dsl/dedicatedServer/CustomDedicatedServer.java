package com.manwe.dsl.dedicatedServer;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.connectionRouting.RegionRouter;
import com.manwe.dsl.dedicatedServer.proxy.RemotePlayerList;
import com.manwe.dsl.dedicatedServer.worker.packets.WorkerBoundReqSyncTimePacket;
import com.manwe.dsl.dedicatedServer.worker.packets.WorkerBoundSyncTimePacket;
import com.manwe.dsl.mixinExtension.SetConnectionIntf;
import com.manwe.dsl.arbiter.ArbiterClient;
import com.manwe.dsl.arbiter.ConnectionInfo;
import com.manwe.dsl.config.DSLServerConfigs;
import com.manwe.dsl.dedicatedServer.worker.LocalPlayerList;
import com.manwe.dsl.mixin.accessors.DedicatedServerAccessor;
import com.manwe.dsl.mixin.accessors.EntityAccessor;
import com.mojang.datafixers.DataFixer;
import net.minecraft.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.*;
import net.minecraft.server.dedicated.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.server.rcon.thread.QueryThreadGs4;
import net.minecraft.server.rcon.thread.RconThread;
import net.minecraft.util.debugchart.*;
import net.minecraft.util.monitoring.jmx.MinecraftServerStatistics;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

public class CustomDedicatedServer extends DedicatedServer {

    URI arbiterUri = URI.create(DSLServerConfigs.ARBITER_ADDR.get());
    private final boolean isProxy = DSLServerConfigs.IS_PROXY.get();
    private final int workerSize = DSLServerConfigs.WORKER_SIZE.get();
    public int workerId = DSLServerConfigs.WORKER_ID.get();
    public boolean logTiks = DSLServerConfigs.LOG_TICK_TIME.get();
    ArbiterClient arbiterClient = new ArbiterClient(arbiterUri);
    private ArbiterClient.ArbiterRes topology;
    private static final int SHARD_MAX_ENTITIES = 1_000_000;

    private final Map<String,Long> levelTime = new ConcurrentHashMap<>();
    private final AtomicInteger timeSugestions = new AtomicInteger();
    private RegionRouter router = null;

    public Map<Integer, Float> workersMSPT = new ConcurrentHashMap<>();
    public Map<Integer, List<Long>> workersNanoTicks = new HashMap<>();

    public CustomDedicatedServer(Thread pServerThread, LevelStorageSource.LevelStorageAccess pStorageSource, PackRepository pPackRepository, WorldStem pWorldStem, DedicatedServerSettings pSettings, DataFixer pFixerUpper, Services pServices, ChunkProgressListenerFactory pProgressListenerFactory) {
        super(pServerThread, pStorageSource, pPackRepository, pWorldStem, pSettings, pFixerUpper, pServices, pProgressListenerFactory);

        //Scuffed stuff here, convert the connection variable to mutable and set it after initialization by MinecraftServer
        //Interface used only to compile
        ((SetConnectionIntf) this).setConnection(new CustomServerConnectionListener(this));
    }

    @Override
    public boolean initServer() throws IOException {
        if(DSLServerConfigs.USE_ARBITER.get()){
            try {
                topology = arbiterClient.fetch();
                System.out.println("Port from Arbiter: "+topology.port);
            } catch (Exception ex) {
                DistributedServerLevels.LOGGER.error("Unexpected Error",ex);
                throw new RuntimeException("Arbiter unavailable cannot fetch port and role");
            }
        }else {
            topology = new ArbiterClient.ArbiterRes(DSLServerConfigs.PORT.get(),DSLServerConfigs.getConnectionAddresses());
        }


        if(!isProxy){
            EntityAccessor.getEntityCounter().set((workerId - 1) * SHARD_MAX_ENTITIES); //Set exclusive entity ids for each worker
        }

        Thread thread = new Thread("Server console handler") {
            @Override
            public void run() {
                if (net.neoforged.neoforge.server.console.TerminalHandler.handleCommands(CustomDedicatedServer.this)) return;
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

                String s1;
                try {
                    while (!CustomDedicatedServer.this.isStopped() && CustomDedicatedServer.this.isRunning() && (s1 = bufferedreader.readLine()) != null) {
                        CustomDedicatedServer.this.handleConsoleInput(s1, CustomDedicatedServer.this.createCommandSourceStack());
                    }
                } catch (IOException ioexception1) {
                    DistributedServerLevels.LOGGER.error("Exception handling console input", ioexception1);
                }
            }
        };

        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(DistributedServerLevels.LOGGER));
        thread.start();
        DistributedServerLevels.LOGGER.info("Starting minecraft server version {}", SharedConstants.getCurrentVersion().getName());
        if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L) {
            DistributedServerLevels.LOGGER.warn("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
        }

        DistributedServerLevels.LOGGER.info("Loading properties");
        DedicatedServerProperties dedicatedserverproperties = ((DedicatedServerAccessor) this).getSettings().getProperties();
        if (this.isSingleplayer()) {
            this.setLocalIp("127.0.0.1");
        } else {
            this.setUsesAuthentication(dedicatedserverproperties.onlineMode);
            this.setPreventProxyConnections(dedicatedserverproperties.preventProxyConnections);
            this.setLocalIp(dedicatedserverproperties.serverIp);
        }

        this.setPvpAllowed(dedicatedserverproperties.pvp);
        this.setFlightAllowed(dedicatedserverproperties.allowFlight);
        this.setMotd(dedicatedserverproperties.motd);
        super.setPlayerIdleTimeout(dedicatedserverproperties.playerIdleTimeout.get());
        this.setEnforceWhitelist(dedicatedserverproperties.enforceWhitelist);
        this.worldData.setGameType(dedicatedserverproperties.gamemode);
        DistributedServerLevels.LOGGER.info("Default game type: {}", dedicatedserverproperties.gamemode);
        InetAddress inetaddress = null;
        if (!this.getLocalIp().isEmpty()) {
            inetaddress = InetAddress.getByName(this.getLocalIp());
        }

        //SET PORT
        this.setPort(topology.port);

        this.initializeKeyPair();
        DistributedServerLevels.LOGGER.info("Starting Minecraft server on {}:{}", this.getLocalIp().isEmpty() ? "*" : this.getLocalIp(), this.getPort());

        try {

            if(this.getConnection() instanceof CustomServerConnectionListener pServerConnectionListener){ //Listener custom
                pServerConnectionListener.startTcpServerListener(inetaddress,this.getPort());
            } else { //Default listener
                System.out.println("Connection is not an instance of CustomServerConnectionListener");
                this.getConnection().startTcpServerListener(inetaddress, this.getPort());
            }

        } catch (IOException ioexception) {
            DistributedServerLevels.LOGGER.warn("**** FAILED TO BIND TO PORT!");
            DistributedServerLevels.LOGGER.warn("The exception was: {}", ioexception.toString());
            DistributedServerLevels.LOGGER.warn("Perhaps a server is already running on that port?");
            return false;
        }

        if (!this.usesAuthentication()) {
            DistributedServerLevels.LOGGER.warn("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
            DistributedServerLevels.LOGGER.warn("The server will make no attempt to authenticate usernames. Beware.");
            DistributedServerLevels.LOGGER.warn(
                    "While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose."
            );
            DistributedServerLevels.LOGGER.warn("To change this, set \"online-mode\" to \"true\" in the server.properties file.");
        }

        if (this.convertOldUsers()) {
            this.getProfileCache().save();
        }

        if (!OldUsersConverter.serverReadyAfterUserconversion(this)) {
            return false;
        } else {
            if(isProxy){
                this.router = new RegionRouter(this);
                //Set RemotePlayerList
                this.setPlayerList(new RemotePlayerList(this, this.registries(), this.playerDataStorage, router));
            }else {
                //Set LocalPlayerList
                this.setPlayerList(new LocalPlayerList(this, this.registries(), this.playerDataStorage));
            }

            ((DedicatedServerAccessor)this).setDebugSampleSubscriptionTracker(new DebugSampleSubscriptionTracker(this.getPlayerList()));
            ((DedicatedServerAccessor)this).setTickTimeLogger(new RemoteSampleLogger(
                TpsDebugDimensions.values().length, ((DedicatedServerAccessor)this).getDebugSampleSubscriptionTracker(), RemoteDebugSampleType.TICK_TIME
            ));
            long i = Util.getNanos();
            SkullBlockEntity.setup(this.services, this);
            GameProfileCache.setUsesAuthentication(this.usesAuthentication());
            net.neoforged.neoforge.server.ServerLifecycleHooks.handleServerAboutToStart(this);
            DistributedServerLevels.LOGGER.info("Preparing level \"{}\"", this.getLevelIdName());
            this.loadLevel();
            long j = Util.getNanos() - i;
            String s = String.format(Locale.ROOT, "%.3fs", (double)j / 1.0E9);
            DistributedServerLevels.LOGGER.info("Done ({})! For help, type \"help\"", s);
            this.nextTickTimeNanos = Util.getNanos(); // Neo: Update server time to prevent watchdog/spaming during long load.
            if (dedicatedserverproperties.announcePlayerAchievements != null) {
                this.getGameRules().getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(dedicatedserverproperties.announcePlayerAchievements, this);
            }

            if (dedicatedserverproperties.enableQuery) {
                DistributedServerLevels.LOGGER.info("Starting GS4 status listener");
                ((DedicatedServerAccessor)this).setQueryThreadGs4(QueryThreadGs4.create(this));
            }

            if (dedicatedserverproperties.enableRcon) {
                DistributedServerLevels.LOGGER.info("Starting remote control listener");
                ((DedicatedServerAccessor)this).setRconThread(RconThread.create(this));
            }

            if (this.getMaxTickLength() > 0L) {
                Thread thread1 = new Thread(new ServerWatchdog(this));
                thread1.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandlerWithName(DistributedServerLevels.LOGGER));
                thread1.setName("Server Watchdog");
                thread1.setDaemon(true);
                thread1.start();
            }

            if (dedicatedserverproperties.enableJmxMonitoring) {
                MinecraftServerStatistics.registerJmxMonitoring(this);
                DistributedServerLevels.LOGGER.info("JMX monitoring enabled");
            }

            if (net.neoforged.neoforge.common.NeoForgeConfig.SERVER.advertiseDedicatedServerToLan.get()) {
                ((DedicatedServerAccessor)this).setDediLanPinger(new net.minecraft.client.server.LanServerPinger(this.getMotd(), String.valueOf(this.getServerPort())));
                ((DedicatedServerAccessor)this).getDediLanPinger().start();
            }

            net.neoforged.neoforge.server.ServerLifecycleHooks.handleServerStarting(this);
            return true;
        }
    }

    @Override
    public void tickServer(@NotNull BooleanSupplier pHasTimeLeft) {
        super.tickServer(pHasTimeLeft);

        if(isProxy() && getTickCount() % 200 == 0){
            router.broadCast(new WorkerBoundReqSyncTimePacket());//Request each worker level times
        }
    }

    @Override
    public void tickChildren(@NotNull BooleanSupplier pHasTimeLeft) {
        super.tickChildren(pHasTimeLeft); //Delete level ticking
    }

    public boolean isProxy(){
        return DSLServerConfigs.IS_PROXY.get();
    }

    /**
     * Returns the Address of the available workers. Returns null if this is a worker
     */
    public List<ConnectionInfo> getWorkers(){
        if(DSLServerConfigs.IS_PROXY.get()){
            return topology.connections;
        }
        return null;
    }

    /**
     * <h2>Proxy</h2>
     * Add worker suggestion
     * @param sugestedLevelTime time map
     */
    public void addSugestedLevelTime(Map<String,Long> sugestedLevelTime){
        for (Map.Entry<String, Long> entry : sugestedLevelTime.entrySet()) {
            levelTime.merge(entry.getKey(), entry.getValue(), Math::max);
        }

        if(timeSugestions.incrementAndGet() == workerSize){
            if(timeSugestions.compareAndSet(workerSize,0)){
                router.broadCast(new WorkerBoundSyncTimePacket(this.levelTime)); //Update each worker level times
            }
        }
    }

    /**
     * <h2>Worker</h2>
     * Set all serverLevels of this worker to specific time
     * @param levelTime time map
     */
    public void syncAllServerLevelTime(Map<String,Long> levelTime){
        if(isProxy()) return; //Proxy has no levels
        for (Map.Entry<String,Long> entry : levelTime.entrySet()){
            ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(entry.getKey()));
            ServerLevel level = this.getLevel(dimKey);
            if(level != null){
                level.setDayTime(entry.getValue());
            }
        }
        System.out.println("Time Synced with proxy");
    }

    /**
     * <h2>Worker</h2>
     * @return time of day for each ServeLevel
     */
    public Map<String,Long> getAllLevelsTime(){
        System.out.println("Requested Time");
        Map<String,Long> sugestedLevelTime = new HashMap<>();
        for(ServerLevel level : getAllLevels()){
            sugestedLevelTime.put(level.dimension().location().toString(),level.getDayTime());
        }
        return sugestedLevelTime;
    }

    @Override
    public void setId(@NotNull String pServerId) {
        super.setId(pServerId);
    }
}
