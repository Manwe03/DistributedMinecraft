package com.manwe.dsl.dedicatedServer;

import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.*;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerInformationPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerLoginPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.chunkloading.ProxyBoundFakePlayerMovePacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.login.ProxyBoundLevelInformationPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.login.ProxyBoundPlayerInitACKPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.transfer.ProxyBoundEntityTransferPacket;
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
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundEntityTransferPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerEndTransferPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.transfer.WorkerBoundPlayerTransferPacket;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.ProtocolInfoBuilder;

public class InternalGameProtocols {
    public static final ProtocolInfo.Unbound<WorkerListener, FriendlyByteBuf> SERVERBOUND_TEMPLATE = ProtocolInfoBuilder.serverboundProtocol(
            ConnectionProtocol.PLAY, consumer -> consumer
                    .addPacket(InternalPacketTypes.PROXY_WORKER_PACKET_CONTAINER, WorkerBoundContainerPacket.STREAM_CODEC)

                    .addPacket(InternalPacketTypes.PROXY_WORKER_LEVEL_INFORMATION, WorkerBoundRequestLevelInformationPacket.STREAM_CODEC)

                    .addPacket(InternalPacketTypes.PROXY_WORKER_PLAYER_LOGIN, WorkerBoundPlayerLoginPacket.STREAM_CODEC)
                    .addPacket(InternalPacketTypes.PROXY_WORKER_PLAYER_LOGIN_ACK, WorkerBoundPlayerLoginACKPacket.STREAM_CODEC)
                    .addPacket(InternalPacketTypes.PROXY_WORKER_PLAYER_DISCONNECT, WorkerBoundPlayerDisconnectPacket.STREAM_CODEC)
                    .addPacket(InternalPacketTypes.PROXY_WORKER_PLAYER_TRANSFER, WorkerBoundPlayerTransferPacket.STREAM_CODEC)
                    .addPacket(InternalPacketTypes.PROXY_WORKER_PLAYER_END_TRANSFER, WorkerBoundPlayerEndTransferPacket.STREAM_CODEC)

                    .addPacket(InternalPacketTypes.PROXY_WORKER_ENTITY_TRANSFER, WorkerBoundEntityTransferPacket.STREAM_CODEC)

                    .addPacket(InternalPacketTypes.PROXY_WORKER_FAKE_PLAYER_LOGIN, WorkerBoundFakePlayerLoginPacket.STREAM_CODEC)
                    .addPacket(InternalPacketTypes.PROXY_WORKER_FAKE_PLAYER_MOVE, WorkerBoundFakePlayerMovePacket.STREAM_CODEC)
                    .addPacket(InternalPacketTypes.PROXY_WORKER_FAKE_PLAYER_INFORMATION, WorkerBoundFakePlayerInformationPacket.STREAM_CODEC)

                    .addPacket(InternalPacketTypes.PROXY_WORKER_CHAT_MESSAGE, WorkerBoundChatPacket.STREAM_CODEC)
    );
    public static final ProtocolInfo<WorkerListener> SERVERBOUND = SERVERBOUND_TEMPLATE.bind(FriendlyByteBuf::new);

    public static final ProtocolInfo.Unbound<ProxyListener, FriendlyByteBuf> CLIENTBOUND_TEMPLATE = ProtocolInfoBuilder.clientboundProtocol(
            ConnectionProtocol.PLAY, consumer -> consumer
                    .addPacket(InternalPacketTypes.WORKER_PROXY_PACKET_CONTAINER, ProxyBoundContainerPacket.STREAM_CODEC)

                    .addPacket(InternalPacketTypes.WORKER_PROXY_LEVEL_INFORMATION, ProxyBoundLevelInformationPacket.STREAM_CODEC)
                    .addPacket(InternalPacketTypes.WORKER_PROXY_SAVE_PLAYER_STATE, ProxyBoundSavePlayerStatePacket.STREAM_CODEC)

                    .addPacket(InternalPacketTypes.WORKER_PROXY_PLAYER_LOGIN_ACK, ProxyBoundPlayerInitACKPacket.STREAM_CODEC)
                    .addPacket(InternalPacketTypes.WORKER_PROXY_PLAYER_TRANSFER, ProxyBoundPlayerTransferPacket.STREAM_CODEC)
                    .addPacket(InternalPacketTypes.WORKER_PROXY_PLAYER_TRANSFER_ACK, ProxyBoundPlayerTransferACKPacket.STREAM_CODEC)

                    .addPacket(InternalPacketTypes.WORKER_PROXY_ENTITY_TRANSFER, ProxyBoundEntityTransferPacket.STREAM_CODEC)

                    .addPacket(InternalPacketTypes.WORKER_PROXY_FAKE_PLAYER_LOGIN, ProxyBoundFakePlayerLoginPacket.STREAM_CODEC)
                    .addPacket(InternalPacketTypes.WORKER_PROXY_FAKE_PLAYER_MOVE, ProxyBoundFakePlayerMovePacket.STREAM_CODEC)
                    .addPacket(InternalPacketTypes.WORKER_PROXY_FAKE_PLAYER_INFORMATION, ProxyBoundFakePlayerInformationPacket.STREAM_CODEC)
                    .addPacket(InternalPacketTypes.WORKER_PROXY_FAKE_PLAYER_DISCONNECT, ProxyBoundFakePlayerDisconnectPacket.STREAM_CODEC)
    );

    public static final ProtocolInfo<ProxyListener> CLIENTBOUND = CLIENTBOUND_TEMPLATE.bind(FriendlyByteBuf::new);
}
