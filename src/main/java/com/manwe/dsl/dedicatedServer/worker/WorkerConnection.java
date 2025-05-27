package com.manwe.dsl.dedicatedServer.worker;

import com.manwe.dsl.DistributedServerLevels;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.jetbrains.annotations.Nullable;

public class WorkerConnection extends Connection {
    public WorkerConnection(PacketFlow pReceiving) {
        super(pReceiving);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext pContext, Throwable pException){
        //TODO hay que ver como manejar los errores que normalmente van al cliente
        DistributedServerLevels.LOGGER.error("Fake Worker Player Connection Error", pException);
    }
}
