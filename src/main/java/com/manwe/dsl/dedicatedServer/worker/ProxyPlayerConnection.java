package com.manwe.dsl.dedicatedServer.worker;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.ProxyBoundContainerPacket;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ProxyPlayerConnection extends Connection {

    private final UUID playerId;
    private final RegistryAccess workerRegistryAccess;

    public ProxyPlayerConnection(PacketFlow pReceiving, UUID playerId, RegistryAccess workerRegistryAccess) {
        super(pReceiving);
        this.playerId = playerId;
        this.workerRegistryAccess = workerRegistryAccess;
    }

    /**
     * <img src="https://i.imgur.com/zXZslJX.jpeg">
     * <P>Wrap all messages in the ProxyBoundContainerPacket to be sent through the same ChannelPipeline</P>
     * <P>All packets should be of type Packet<ClientGamePacketListener></P>
     */
    @Override
    public void send(Packet<?> pPacket, @Nullable PacketSendListener pListener, boolean pFlush) {
        try {
            ProxyBoundContainerPacket newPacket = new ProxyBoundContainerPacket(playerId, (Packet<ClientGamePacketListener>) pPacket, this.workerRegistryAccess);

            super.send(newPacket, pListener, pFlush);
        }catch (Exception e){
            DistributedServerLevels.LOGGER.warn("Worker tried to send an unknown packet", e);
        }
    }
}
