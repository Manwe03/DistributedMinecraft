package com.manwe.dsl.dedicatedServer.proxy.back;

import com.manwe.dsl.DistributedServerLevels;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;

public class ProxyWorkerConnection extends Connection {
    public ProxyWorkerConnection(PacketFlow pReceiving) {
        super(pReceiving);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext pContext, Throwable pException){
        //TODO hay que ver como manejar los errores que normalmente van al cliente
        DistributedServerLevels.LOGGER.error("Fake Worker Player Connection Error", pException);
    }
}
