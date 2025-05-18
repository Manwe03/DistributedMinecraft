package com.manwe.dsl.dedicatedServer;

import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.PlayerInitPacket;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.WorkerProxyPacket;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import com.manwe.dsl.dedicatedServer.worker.packets.ProxyWorkerPacket;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.ProtocolInfoBuilder;

public class InternalGameProtocols {
    public static final ProtocolInfo.Unbound<WorkerListener, FriendlyByteBuf> SERVERBOUND_TEMPLATE = ProtocolInfoBuilder.serverboundProtocol(
            ConnectionProtocol.PLAY, consumer ->
                    consumer.addPacket(InternalPacketTypes.PROXY_WORKER_PACKET_CONTAINER, ProxyWorkerPacket.STREAM_CODEC)
                    .addPacket(InternalPacketTypes.PROXY_WORKER_CLIENT_LOGIN, PlayerInitPacket.STREAM_CODEC)
    );
    public static final ProtocolInfo<WorkerListener> SERVERBOUND = SERVERBOUND_TEMPLATE.bind(FriendlyByteBuf::new);

      //De momento los worker no devuelven nada
    public static final ProtocolInfo.Unbound<ProxyListener, FriendlyByteBuf> CLIENTBOUND_TEMPLATE = ProtocolInfoBuilder.clientboundProtocol(
            ConnectionProtocol.PLAY, consumer ->
                      consumer.addPacket(InternalPacketTypes.WORKER_PROXY_PACKET_CONTAINER, WorkerProxyPacket.STREAM_CODEC)
    );

    public static final ProtocolInfo<ProxyListener> CLIENTBOUND = CLIENTBOUND_TEMPLATE.bind(FriendlyByteBuf::new);
}
