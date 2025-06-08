package com.manwe.dsl.dedicatedServer;

import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.*;
import com.manwe.dsl.dedicatedServer.worker.packets.*;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;

public class InternalPacketTypes {
    public static final PacketType<WorkerBoundPlayerInitPacket> PROXY_WORKER_CLIENT_LOGIN = createServerbound("worker_player_login");
    public static final PacketType<WorkerBoundPlayerInitACKPacket> PROXY_WORKER_PLAYER_INIT_ACK = createServerbound("worker_player_init_ack");
    public static final PacketType<WorkerBoundPlayerDisconnectPacket> PROXY_WORKER_CLIENT_DISCONNECT = createServerbound("worker_player_disconnect");
    public static final PacketType<WorkerBoundContainerPacket> PROXY_WORKER_PACKET_CONTAINER = createServerbound("worker_packet_container");
    public static final PacketType<WorkerBoundPlayerTransferPacket> PROXY_WORKER_PLAYER_TRANSFER = createServerbound("worker_player_transfer");


    private static <T extends Packet<WorkerListener>> PacketType<T> createServerbound(String pName) {
        return new PacketType<>(PacketFlow.SERVERBOUND, ResourceLocation.withDefaultNamespace(pName));
    }

    public static final PacketType<ProxyBoundContainerPacket> WORKER_PROXY_PACKET_CONTAINER = createClientbound("proxy_packet_container");
    public static final PacketType<ProxyBoundPlayerTransferPacket> WORKER_PROXY_PLAYER_TRANSFER = createClientbound("proxy_player_transfer");
    public static final PacketType<ProxyBoundPlayerTransferACKPacket> WORKER_PROXY_PLAYER_TRANSFER_ACK = createClientbound("proxy_player_transfer_ack");
    public static final PacketType<ProxyBoundSavePlayerStatePacket> WORKER_PROXY_SAVE_PLAYER_STATE = createClientbound("proxy_save_player_state");
    public static final PacketType<ProxyBoundPlayerInitACKPacket> WORKER_PROXY_PLAYER_INIT_ACK = createClientbound("proxy_player_init_ack");

    private static <T extends Packet<ProxyListener>> PacketType<T> createClientbound(String pId) {
        return new PacketType<>(PacketFlow.CLIENTBOUND, ResourceLocation.withDefaultNamespace(pId));
    }
}
