package com.manwe.dsl.dedicatedServer;

import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.WorkerBoundPlayerInitPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.WorkerBoundPlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.WorkerProxyPacket;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import com.manwe.dsl.dedicatedServer.worker.packets.ProxyWorkerPacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;

public class InternalPacketTypes {
    public static final PacketType<WorkerBoundPlayerInitPacket> PROXY_WORKER_CLIENT_LOGIN = createServerbound("worker_player_login");
    public static final PacketType<WorkerBoundPlayerDisconnectPacket> PROXY_WORKER_CLIENT_DISCONNECT = createServerbound("worker_player_disconnect");
    public static final PacketType<ProxyWorkerPacket> PROXY_WORKER_PACKET_CONTAINER = createServerbound("worker_packet_container");

    private static <T extends Packet<WorkerListener>> PacketType<T> createServerbound(String pName) {
        return new PacketType<>(PacketFlow.SERVERBOUND, ResourceLocation.withDefaultNamespace(pName));
    }

    public static final PacketType<WorkerProxyPacket> WORKER_PROXY_PACKET_CONTAINER = createClientbound("proxy_packet_container");

    private static <T extends Packet<ProxyListener>> PacketType<T> createClientbound(String pId) {
        return new PacketType<>(PacketFlow.CLIENTBOUND, ResourceLocation.withDefaultNamespace(pId));
    }
}
