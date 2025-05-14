package com.manwe.dsl.mixin.accessors;

import net.minecraft.client.server.LanServerPinger;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.rcon.thread.QueryThreadGs4;
import net.minecraft.server.rcon.thread.RconThread;
import net.minecraft.util.debugchart.DebugSampleSubscriptionTracker;
import net.minecraft.util.debugchart.RemoteSampleLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DedicatedServer.class)
public interface DedicatedServerAccessor {
    @Accessor("settings")
    DedicatedServerSettings getSettings();

    @Accessor("debugSampleSubscriptionTracker")
    DebugSampleSubscriptionTracker getDebugSampleSubscriptionTracker();

    @Accessor("debugSampleSubscriptionTracker")
    void setDebugSampleSubscriptionTracker(DebugSampleSubscriptionTracker tracker);

    @Accessor("tickTimeLogger")
    void setTickTimeLogger(RemoteSampleLogger logger);

    @Accessor("queryThreadGs4")
    void setQueryThreadGs4(QueryThreadGs4 queryThreadGs4);

    @Accessor("rconThread")
    void setRconThread(RconThread rconThread);

    @Accessor("dediLanPinger")
    void setDediLanPinger(LanServerPinger dediLanPinger);

    @Accessor("dediLanPinger")
    LanServerPinger getDediLanPinger();

}
