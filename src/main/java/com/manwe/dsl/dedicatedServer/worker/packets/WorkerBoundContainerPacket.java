package com.manwe.dsl.dedicatedServer.worker.packets;

import com.manwe.dsl.dedicatedServer.ContanierPacketProtocolCache;
import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListener;
import net.minecraft.network.*;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WorkerBoundContainerPacket implements Packet<WorkerListener> {

    private final UUID playerId;
    private final Packet<? extends ServerCommonPacketListener> payload;

    public static final StreamCodec<FriendlyByteBuf, WorkerBoundContainerPacket> STREAM_CODEC = Packet.codec(
            WorkerBoundContainerPacket::write, WorkerBoundContainerPacket::new
    );

    public WorkerBoundContainerPacket(UUID playerId, Packet<? extends ServerCommonPacketListener> payload){
        this.playerId = playerId;
        this.payload = payload;
    }

    private WorkerBoundContainerPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();

        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if(currentServer == null) throw new RuntimeException("MinecraftServer is null");
        ProtocolInfo<ServerGamePacketListener> proto = ContanierPacketProtocolCache.serverbound(currentServer.registryAccess());
        //ProtocolInfo<ServerGamePacketListener> protocolInfo = GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(currentServer.registryAccess(), ConnectionType.OTHER));

        FriendlyByteBuf payloadBuf = new FriendlyByteBuf(buf.readBytes(buf.readableBytes()));
        this.payload = (Packet<? extends ServerCommonPacketListener>) proto.codec().decode(payloadBuf);

    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);

        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if(currentServer == null) throw new RuntimeException("MinecraftServer is null");

        ProtocolInfo<ServerGamePacketListener> proto = ContanierPacketProtocolCache.serverbound(currentServer.registryAccess());

        proto.codec().encode(buf, (Packet<? super ServerGamePacketListener>) this.payload);
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
    public @NotNull PacketType<? extends Packet<WorkerListener>> type() {
        return InternalPacketTypes.PROXY_WORKER_PACKET_CONTAINER;
    }
}
