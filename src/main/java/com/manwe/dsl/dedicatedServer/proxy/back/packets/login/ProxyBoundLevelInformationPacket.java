package com.manwe.dsl.dedicatedServer.proxy.back.packets.login;

import com.manwe.dsl.dedicatedServer.InternalPacketTypes;
import com.manwe.dsl.dedicatedServer.proxy.back.listeners.ProxyListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import org.jetbrains.annotations.NotNull;

public class ProxyBoundLevelInformationPacket implements Packet<ProxyListener> {

    public static final StreamCodec<FriendlyByteBuf, ProxyBoundLevelInformationPacket> STREAM_CODEC =
            Packet.codec(ProxyBoundLevelInformationPacket::write, ProxyBoundLevelInformationPacket::new);

    private final BlockPos defaultSpawnPos;

    public ProxyBoundLevelInformationPacket(BlockPos defaultSpawnPos) {
        this.defaultSpawnPos = defaultSpawnPos;
    }

    public ProxyBoundLevelInformationPacket(FriendlyByteBuf buf) {
        this.defaultSpawnPos = buf.readBlockPos();
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.defaultSpawnPos);
    }

    public BlockPos getDefaultSpawnPos() {
        return defaultSpawnPos;
    }

    @Override
    public @NotNull PacketType<? extends Packet<ProxyListener>> type() { return InternalPacketTypes.WORKER_PROXY_LEVEL_INFORMATION; }

    /**
     * Passes this Packet on to the PacketListener for processing.
     *
     * @param pHandler
     */
    @Override
    public void handle(ProxyListener pHandler) {
        pHandler.handleLevelInformation(this);
    }
}
