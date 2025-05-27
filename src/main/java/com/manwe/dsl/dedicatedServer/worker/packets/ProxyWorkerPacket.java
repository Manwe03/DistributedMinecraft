package com.manwe.dsl.dedicatedServer.worker.packets;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import com.mojang.serialization.Decoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.flow.FlowControlHandler;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.*;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.UUID;

public class ProxyWorkerPacket implements Packet<WorkerListener> {

    private final UUID playerId;
    private final Packet<? extends ServerCommonPacketListener> payload;

    public static final StreamCodec<FriendlyByteBuf, ProxyWorkerPacket> STREAM_CODEC = Packet.codec(
            ProxyWorkerPacket::write, ProxyWorkerPacket::new
    );

    public ProxyWorkerPacket(UUID playerId, Packet<? extends ServerCommonPacketListener> payload){
        this.playerId = playerId;
        this.payload = payload;
    }

    private ProxyWorkerPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();

        int length = buf.readableBytes();
        ByteBuf payloadRaw = buf.readBytes(length);

        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if(currentServer == null) throw new RuntimeException("MinecraftServer is null");
        ProtocolInfo<ServerGamePacketListener> protocolInfo = GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(currentServer.registryAccess(), ConnectionType.OTHER));

        //TODO si se puede crear una sola instancia del EmbeddedChannel en vez de una por serialización y deserialización
        EmbeddedChannel channel = new EmbeddedChannel(
                new Varint21FrameDecoder(null),
                new FlowControlHandler(),
                new PacketDecoder<>(protocolInfo)
        );
        channel.writeInbound(payloadRaw);
        this.payload = channel.readInbound();
        if(payload == null) throw new RuntimeException("P->W Decode: Payload is null");
        channel.finish();
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);

        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if(currentServer == null) throw new RuntimeException("MinecraftServer is null");
        ProtocolInfo<ServerGamePacketListener> protocolInfo = GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(currentServer.registryAccess(), ConnectionType.OTHER));

        //TODO si se puede crear una sola instancia del EmbeddedChannel en vez de una por serialización y deserialización
        EmbeddedChannel channel = new EmbeddedChannel(
                new Varint21LengthFieldPrepender(),
                new PacketEncoder<>(protocolInfo)
        );
        channel.writeOutbound(this.payload);
        if(payload == null) throw new RuntimeException("P->W Encode: Payload is null");
        ByteBuf encoded = channel.readOutbound();
        buf.writeBytes(encoded);
        channel.finish();
    }

    public Packet<? extends ServerCommonPacketListener> getPayload(){
        return this.payload;
    }

    public UUID getPlayerId(){
        return this.playerId;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     *
     * @param pHandler
     */
    @Override
    public void handle(WorkerListener pHandler) {
        pHandler.handleProxyWorkerPacket(this);
    }

    @Override
    public PacketType<? extends Packet<WorkerListener>> type() {
        return InternalPacketTypes.PROXY_WORKER_PACKET_CONTAINER;
    }
}
