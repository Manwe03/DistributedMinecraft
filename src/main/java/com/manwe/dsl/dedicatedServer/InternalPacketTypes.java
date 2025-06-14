package com.manwe.dsl.dedicatedServer;

import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.*;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerInformationPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerLoginPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerMovePacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.login.ProxyBoundLevelInformationPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.login.ProxyBoundPlayerInitACKPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.transfer.ProxyBoundPlayerTransferACKPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.transfer.ProxyBoundPlayerTransferPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.*;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import com.manwe.dsl.dedicatedServer.worker.packets.chunkloading.WorkerBoundFakePlayerInformationPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.chunkloading.WorkerBoundFakePlayerLoginPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.chunkloading.WorkerBoundFakePlayerMovePacket;
import com.manwe.dsl.dedicatedServer.worker.packets.login.WorkerBoundRequestLevelInformationPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.login.WorkerBoundPlayerLoginACKPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.login.WorkerBoundPlayerLoginPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerEndTransferPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerTransferPacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;

public class InternalPacketTypes {
    public static final PacketType<WorkerBoundContainerPacket> PROXY_WORKER_PACKET_CONTAINER = createServerbound("worker_packet_container");

    public static final PacketType<WorkerBoundRequestLevelInformationPacket> PROXY_WORKER_LEVEL_INFORMATION = createServerbound("worker_level_information");

    public static final PacketType<WorkerBoundPlayerLoginPacket> PROXY_WORKER_PLAYER_LOGIN = createServerbound("worker_player_login");
    public static final PacketType<WorkerBoundPlayerLoginACKPacket> PROXY_WORKER_PLAYER_LOGIN_ACK = createServerbound("worker_player_login_ack");
    public static final PacketType<WorkerBoundPlayerDisconnectPacket> PROXY_WORKER_PLAYER_DISCONNECT = createServerbound("worker_player_disconnect");
    public static final PacketType<WorkerBoundPlayerTransferPacket> PROXY_WORKER_PLAYER_TRANSFER = createServerbound("worker_player_transfer");
    public static final PacketType<WorkerBoundPlayerEndTransferPacket> PROXY_WORKER_PLAYER_END_TRANSFER = createServerbound("worker_player_end_transfer");

    public static final PacketType<WorkerBoundFakePlayerLoginPacket> PROXY_WORKER_FAKE_PLAYER_LOGIN = createServerbound("worker_fake_player_login");
    public static final PacketType<WorkerBoundFakePlayerMovePacket> PROXY_WORKER_FAKE_PLAYER_MOVE = createServerbound("worker_fake_player_move");
    public static final PacketType<WorkerBoundFakePlayerInformationPacket> PROXY_WORKER_FAKE_PLAYER_INFORMATION = createServerbound("worker_fake_player_information");

    private static <T extends Packet<WorkerListener>> PacketType<T> createServerbound(String pName) {
        return new PacketType<>(PacketFlow.SERVERBOUND, ResourceLocation.withDefaultNamespace(pName));
    }

    public static final PacketType<ProxyBoundContainerPacket> WORKER_PROXY_PACKET_CONTAINER = createClientbound("proxy_packet_container");

    public static final PacketType<ProxyBoundLevelInformationPacket> WORKER_PROXY_LEVEL_INFORMATION = createClientbound("proxy_level_information");
    public static final PacketType<ProxyBoundSavePlayerStatePacket> WORKER_PROXY_SAVE_PLAYER_STATE = createClientbound("proxy_save_player_state");

    public static final PacketType<ProxyBoundPlayerInitACKPacket> WORKER_PROXY_PLAYER_LOGIN_ACK = createClientbound("proxy_player_login_ack");
    public static final PacketType<ProxyBoundPlayerTransferPacket> WORKER_PROXY_PLAYER_TRANSFER = createClientbound("proxy_player_transfer");
    public static final PacketType<ProxyBoundPlayerTransferACKPacket> WORKER_PROXY_PLAYER_TRANSFER_ACK = createClientbound("proxy_player_transfer_ack");

    public static final PacketType<ProxyBoundFakePlayerLoginPacket> WORKER_PROXY_FAKE_PLAYER_LOGIN = createClientbound("proxy_fake_player_login");
    public static final PacketType<ProxyBoundFakePlayerMovePacket> WORKER_PROXY_FAKE_PLAYER_MOVE = createClientbound("proxy_fake_player_move");
    public static final PacketType<ProxyBoundFakePlayerInformationPacket> WORKER_PROXY_FAKE_PLAYER_INFORMATION = createClientbound("proxy_fake_player_information");
    public static final PacketType<ProxyBoundFakePlayerDisconnectPacket> WORKER_PROXY_FAKE_PLAYER_DISCONNECT = createClientbound("proxy_fake_player_disconnect");

    private static <T extends Packet<ProxyListener>> PacketType<T> createClientbound(String pId) {
        return new PacketType<>(PacketFlow.CLIENTBOUND, ResourceLocation.withDefaultNamespace(pId));
    }
}
