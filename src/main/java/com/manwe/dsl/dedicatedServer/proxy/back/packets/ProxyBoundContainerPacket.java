package com.manwe.dsl.dedicatedServer.proxy.back.packets;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.*;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.*;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProxyBoundContainerPacket implements Packet<ProxyListener> {

    private final UUID playerId;
    private final Packet<? super ClientGamePacketListener> payload;
    private RegistryAccess workerRegistryAccess = null; //Might be null

    public static final StreamCodec<FriendlyByteBuf, ProxyBoundContainerPacket> STREAM_CODEC = Packet.codec(
            ProxyBoundContainerPacket::write, ProxyBoundContainerPacket::new
    );

    public ProxyBoundContainerPacket(UUID playerId, Packet<ClientGamePacketListener> payload, RegistryAccess workerRegistryAccess) {
        this.playerId = playerId;
        this.payload = payload;
        this.workerRegistryAccess = workerRegistryAccess;
    }

    /* ====================================================================== */
    /*                        --------  L E E R  --------                     */
    /* ====================================================================== */

    public ProxyBoundContainerPacket(FriendlyByteBuf buf) {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if(currentServer == null) throw new RuntimeException("MinecraftServer is null");

        ProtocolInfo<ClientGamePacketListener> proto = GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(currentServer.registryAccess(), ConnectionType.OTHER));

        this.playerId = buf.readUUID();

        FriendlyByteBuf payloadBuf = new FriendlyByteBuf(buf.readBytes(buf.readableBytes()));

        List<Packet<? super ClientGamePacketListener>> list = new ArrayList<>();
        while (payloadBuf.isReadable()) {
            Packet<?> p = proto.codec().decode(payloadBuf);
            if (p == null) throw new IllegalStateException("No se pudo decodificar un paquete PLAY");
            list.add((Packet<? super ClientGamePacketListener>) p);
        }

        // 3. Si llegaron varios → Bundle, si no → único
        this.payload = list.size() == 1 ? list.getFirst() : new ClientboundBundlePacket(list);
    }

    /* ====================================================================== */
    /*                      --------  E S C R I B I R  --------               */
    /* ====================================================================== */

    private void write(FriendlyByteBuf buf) {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if(currentServer == null) throw new RuntimeException("MinecraftServer is null");

        buf.writeUUID(playerId);

        ProtocolInfo<ClientGamePacketListener> proto = GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(currentServer.registryAccess(), ConnectionType.OTHER));

        // Serializamos el/los paquetes directamente con el codec de PLAY
        if (payload instanceof ClientboundBundlePacket bundle) {
            for (Packet<? super ClientGamePacketListener> inner : bundle.subPackets()) {
                proto.codec().encode(buf, inner);
            }
        } else {
            proto.codec().encode(buf, payload);
        }
    }

    public Packet<?> getPayload(){
        return this.payload;
    }

    public UUID getPlayerId(){
        return this.playerId;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     * @param pHandler
     */
    @Override
    public void handle(ProxyListener pHandler) {
        pHandler.handleWorkerProxyPacket(this);
    }

    @Override
    public PacketType<? extends Packet<ProxyListener>> type() {
        return InternalPacketTypes.WORKER_PROXY_PACKET_CONTAINER;
    }
}
