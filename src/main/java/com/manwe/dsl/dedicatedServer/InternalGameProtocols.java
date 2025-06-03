package com.manwe.dsl.dedicatedServer;

import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.*;
import com.manwe.dsl.dedicatedServer.worker.packets.*;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.ProtocolInfoBuilder;

public class InternalGameProtocols {
    public static final ProtocolInfo.Unbound<WorkerListener, FriendlyByteBuf> SERVERBOUND_TEMPLATE = ProtocolInfoBuilder.serverboundProtocol(
            ConnectionProtocol.PLAY, consumer ->
                    consumer.addPacket(InternalPacketTypes.PROXY_WORKER_PACKET_CONTAINER, WorkerBoundContainerPacket.STREAM_CODEC)
                    .addPacket(InternalPacketTypes.PROXY_WORKER_CLIENT_LOGIN, WorkerBoundPlayerInitPacket.STREAM_CODEC)
                    .addPacket(InternalPacketTypes.PROXY_WORKER_CLIENT_DISCONNECT, WorkerBoundPlayerDisconnectPacket.STREAM_CODEC)
                    .addPacket(InternalPacketTypes.PROXY_WORKER_PLAYER_TRANSFER, WorkerBoundPlayerTransferPacket.STREAM_CODEC)
                    .addPacket(InternalPacketTypes.PROXY_WORKER_PLAYER_INIT_ACK, WorkerBoundPlayerInitACKPacket.STREAM_CODEC)
    );
    public static final ProtocolInfo<WorkerListener> SERVERBOUND = SERVERBOUND_TEMPLATE.bind(FriendlyByteBuf::new);

      //De momento los worker no devuelven nada
    public static final ProtocolInfo.Unbound<ProxyListener, FriendlyByteBuf> CLIENTBOUND_TEMPLATE = ProtocolInfoBuilder.clientboundProtocol(
            ConnectionProtocol.PLAY, consumer ->
                      consumer.addPacket(InternalPacketTypes.WORKER_PROXY_PACKET_CONTAINER, ProxyBoundContainerPacket.STREAM_CODEC)
                              .addPacket(InternalPacketTypes.WORKER_PROXY_PLAYER_TRANSFER, ProxyBoundPlayerTransferPacket.STREAM_CODEC)
                              .addPacket(InternalPacketTypes.WORKER_PROXY_PLAYER_TRANSFER_ACK, ProxyBoundPlayerTransferACKPacket.STREAM_CODEC)
                              .addPacket(InternalPacketTypes.WORKER_PROXY_SAVE_PLAYER_STATE, ProxyBoundSavePlayerStatePacket.STREAM_CODEC)
                              .addPacket(InternalPacketTypes.WORKER_PROXY_PLAYER_INIT_ACK, ProxyBoundPlayerInitACKPacket.STREAM_CODEC)
    );

    public static final ProtocolInfo<ProxyListener> CLIENTBOUND = CLIENTBOUND_TEMPLATE.bind(FriendlyByteBuf::new);
}
