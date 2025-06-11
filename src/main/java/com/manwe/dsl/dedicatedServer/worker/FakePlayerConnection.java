package com.manwe.dsl.dedicatedServer.worker;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.ProxyBoundContainerPacket;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FakePlayerConnection extends Connection {
    private final UUID playerId;
    private final RegistryAccess workerRegistryAccess;

    public FakePlayerConnection(PacketFlow pReceiving, UUID playerId, RegistryAccess workerRegistryAccess) {
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
        boolean allow =
                pPacket.type() == GamePacketTypes.CLIENTBOUND_LEVEL_CHUNK_WITH_LIGHT ||
                pPacket.type() == GamePacketTypes.CLIENTBOUND_LIGHT_UPDATE ||
                pPacket.type() == GamePacketTypes.CLIENTBOUND_BLOCK_ENTITY_DATA ||
                pPacket.type() == GamePacketTypes.CLIENTBOUND_SECTION_BLOCKS_UPDATE ||
                pPacket.type() == GamePacketTypes.CLIENTBOUND_FORGET_LEVEL_CHUNK ||
                pPacket.type() == GamePacketTypes.CLIENTBOUND_CHUNK_BATCH_START ||
                pPacket.type() == GamePacketTypes.CLIENTBOUND_CHUNK_BATCH_FINISHED ||
                pPacket.type() == GamePacketTypes.CLIENTBOUND_BUNDLE ||
                pPacket.type() == GamePacketTypes.CLIENTBOUND_BUNDLE_DELIMITER;
        try {

            if(allow){
                //System.out.println("Allowed: "+pPacket.type());
                ProxyBoundContainerPacket newPacket = new ProxyBoundContainerPacket(playerId, (Packet<ClientGamePacketListener>) pPacket, this.workerRegistryAccess);
                super.send(newPacket, pListener, pFlush);
            }else {
                //System.out.println("Fake player tried to send but blocked: "+pPacket.type());
            }
        }catch (Exception e){
            DistributedServerLevels.LOGGER.warn("Worker tried to send an unknown packet", e);
        }
    }
}
