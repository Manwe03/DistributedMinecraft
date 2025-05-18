package com.manwe.dsl.dedicatedServer.proxy;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.SetConnectionIntf;
import com.manwe.dsl.arbiter.ArbiterClient;
import com.manwe.dsl.config.DSLServerConfigs;
import com.manwe.dsl.connectionRouting.RegionRouter;
import com.manwe.dsl.dedicatedServer.worker.LocalPlayerList;
import com.manwe.dsl.mixin.accessors.DedicatedServerAccessor;
import com.mojang.datafixers.DataFixer;
import net.minecraft.*;
import net.minecraft.server.*;
import net.minecraft.server.dedicated.*;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.rcon.thread.QueryThreadGs4;
import net.minecraft.server.rcon.thread.RconThread;
import net.minecraft.util.debugchart.*;
import net.minecraft.util.monitoring.jmx.MinecraftServerStatistics;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class ProxyDedicatedServer extends DedicatedServer {

    URI arbiterUri = URI.create(DSLServerConfigs.ARBITER_ADDR.get());
    ArbiterClient arbiterClient = new ArbiterClient(arbiterUri);
    private DedicatedPlayerList localRemotePlayerListRef;

    private ArbiterClient.ArbiterRes topology;

    public ProxyDedicatedServer(Thread pServerThread, LevelStorageSource.LevelStorageAccess pStorageSource, PackRepository pPackRepository, WorldStem pWorldStem, DedicatedServerSettings pSettings, DataFixer pFixerUpper, Services pServices, ChunkProgressListenerFactory pProgressListenerFactory) {
        super(pServerThread, pStorageSource, pPackRepository, pWorldStem, pSettings, pFixerUpper, pServices, pProgressListenerFactory);

        //Scuffed stuff here, convert the connection variable to mutable and set it after initialization by MinecraftServer
        //Interface used only to compile
        ((SetConnectionIntf) this).setConnection(new ProxyServerConnectionListener(this));
    }

    @Override
    public boolean initServer() throws IOException {
        Thread thread = new Thread("Server console handler") {
            @Override
            public void run() {
                if (net.neoforged.neoforge.server.console.TerminalHandler.handleCommands(ProxyDedicatedServer.this)) return;
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

                String s1;
                try {
                    while (!ProxyDedicatedServer.this.isStopped() && ProxyDedicatedServer.this.isRunning() && (s1 = bufferedreader.readLine()) != null) {
                        ProxyDedicatedServer.this.handleConsoleInput(s1, ProxyDedicatedServer.this.createCommandSourceStack());
                    }
                } catch (IOException ioexception1) {
                    DistributedServerLevels.LOGGER.error("Exception handling console input", (Throwable)ioexception1);
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


        try {
            topology = arbiterClient.fetch();
            this.setPort(topology.port);
            System.out.println("From Arbiter: "+topology.port +" : "+topology.proxy);
        } catch (Exception ex) {
            DistributedServerLevels.LOGGER.error("Unexpected Error",ex);
            throw new RuntimeException("Arbiter unavailable cannot fetch port and role");
        }


        this.initializeKeyPair();
        DistributedServerLevels.LOGGER.info("Starting Minecraft server on {}:{}", this.getLocalIp().isEmpty() ? "*" : this.getLocalIp(), this.getPort());

        try {

            if(this.getConnection() instanceof ProxyServerConnectionListener pServerConnectionListener){ //Listener custom
                pServerConnectionListener.startTcpServerListener(inetaddress,this.getPort());
            } else { //Default listener
                System.out.println("Connection is not an instance of ProxyServerConnectionListener");
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
            //Set RemotePlayerList
            if(topology.proxy){
                localRemotePlayerListRef = new RemotePlayerList(this, this.registries(), this.playerDataStorage);
            }else {
                localRemotePlayerListRef = new LocalPlayerList(this, this.registries(), this.playerDataStorage);
            }
            this.setPlayerList(localRemotePlayerListRef);
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

    public boolean isProxy(){
        return topology.proxy;
    }

    public List<InetSocketAddress> getWorkers(){
        return topology.connections;
    }

    public PlayerList getSpecificPlayerList(){
        return localRemotePlayerListRef;
    }

    @Override
    public void setId(String pServerId) {
        super.setId(pServerId);
    }
}
