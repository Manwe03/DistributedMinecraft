package com.manwe.dsl.mixin.accessors;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.ServerStatsCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerList.class)
public interface PlayerListAccessor {

    @Accessor("server")
    MinecraftServer getServer();

    @Accessor("players")
    List<ServerPlayer> getPlayers();

    @Accessor("playersByUUID")
    Map<UUID, ServerPlayer> getPlayersByUUID();

    @Accessor("stats")
    Map<UUID, ServerStatsCounter> getStats();

    @Accessor("advancements")
    Map<UUID, PlayerAdvancements> getAdvancements();
}
