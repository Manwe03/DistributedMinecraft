package com.manwe.dsl.dedicatedServer;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.neoforged.neoforge.network.connection.ConnectionType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ContanierPacketProtocolCache {
    private static final ConcurrentMap<RegistryAccess, ProtocolInfo<ClientGamePacketListener>> CLIENTBOUND = new ConcurrentHashMap<>();
    private static final ConcurrentMap<RegistryAccess, ProtocolInfo<ServerGamePacketListener>> SERVERBOUND = new ConcurrentHashMap<>();

    public static ProtocolInfo<ClientGamePacketListener> clientbound(RegistryAccess registry) {
        return CLIENTBOUND.computeIfAbsent(registry, reg ->
            GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(reg, ConnectionType.OTHER))
        );
    }

    public static ProtocolInfo<ServerGamePacketListener> serverbound(RegistryAccess registry) {
        return SERVERBOUND.computeIfAbsent(registry, reg ->
            GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(reg, ConnectionType.OTHER))
        );
    }

    //See when this should be invalid
    public static void invalidateClientBound(RegistryAccess registry) {
        CLIENTBOUND.remove(registry);
    }
    //See when this should be invalid
    public static void invalidateServerBound(RegistryAccess registry) {
        SERVERBOUND.remove(registry);
    }
}
