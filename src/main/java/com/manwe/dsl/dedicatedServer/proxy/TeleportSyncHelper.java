package com.manwe.dsl.dedicatedServer.proxy;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class TeleportSyncHelper {

    private TeleportSyncHelper() {}

    private static final Set<UUID> UNSYNCED = ConcurrentHashMap.newKeySet();
    private static final Map<UUID,Integer> PENDING_TPS = new ConcurrentHashMap<>();
    private static final Map<UUID,Deque<ServerboundMovePlayerPacket>> MOVES = new ConcurrentHashMap<>();

    /* ----------- login ----------- */
    public static void markUnsynced(UUID p) { UNSYNCED.add(p); }

    /* ----------- worker ⇒ cliente (cada ClientboundPlayerPositionPacket) ----------- */
    public static void incPendingTeleports(UUID p) {
        PENDING_TPS.merge(p, 1, Integer::sum);
    }

    /* ----------- cliente ⇒ proxy (cada AcceptTeleport) ----------- */
    public static void ackTeleport(UUID p) {
        PENDING_TPS.compute(p, (k,v) -> (v==null || v<=1) ? null : v-1);
        // **NO** tocamos UNSYNCED aquí
    }

    /* ----------- cliente ⇒ proxy (cada MovePlayer) ----------- */
    public static boolean mustQueue(UUID p) {
        return UNSYNCED.contains(p) || PENDING_TPS.containsKey(p);
    }
    public static void queueMove(UUID p, ServerboundMovePlayerPacket pkt) {
        MOVES.computeIfAbsent(p, k -> new ArrayDeque<>()).add(pkt);
    }

    /** Llamar justo antes de reenviar un MovePlayer cuando `mustQueue==false`. */
    public static Deque<ServerboundMovePlayerPacket> drainMoves(UUID p) {
        UNSYNCED.remove(p);              // 1. salimos del modo inicial
        return MOVES.remove(p);          // 2. devuelve cola (puede ser null)
    }

    public static void clear(UUID p){
        UNSYNCED.remove(p); PENDING_TPS.remove(p); MOVES.remove(p);
    }
}