package com.manwe.dsl.dedicatedServer.worker;

import com.manwe.dsl.dedicatedServer.InternalGameProtocols;
import com.manwe.dsl.dedicatedServer.proxy.back.ProxyWorkerConnection;
import com.manwe.dsl.dedicatedServer.proxy.ProxyDedicatedServer;
import com.manwe.dsl.dedicatedServer.proxy.ProxyServerConnectionListener;
import com.manwe.dsl.dedicatedServer.worker.listeners.WorkerListenerImpl;
import com.manwe.dsl.mixin.accessors.ConnectionAccessor;
import io.netty.channel.*;
import io.netty.handler.flow.FlowControlHandler;
import net.minecraft.network.*;
import net.minecraft.network.protocol.PacketFlow;

public class WorkerFrontendInit extends ChannelInitializer<Channel> {

    private final ProxyDedicatedServer server;
    private final ProxyServerConnectionListener listener;

    public WorkerFrontendInit(ProxyDedicatedServer server, ProxyServerConnectionListener listener) {
        this.server = server;
        this.listener = listener;
    }

    @Override
    protected void initChannel(Channel ch) {
        System.out.println("WorkerInit");

        try {ch.config().setOption(ChannelOption.TCP_NODELAY, true);} catch (ChannelException ignored) {}

        //Pipeline
        ChannelPipeline pipeline = ch.pipeline();
        //Codecs
        pipeline.addLast("splitter", new Varint21FrameDecoder(null))
                .addLast(new FlowControlHandler())
                .addLast("decoder", new PacketDecoder<>(InternalGameProtocols.SERVERBOUND)) //Debería decodificar los paquetes PlayerInitPacket
                .addLast("prepender", new Varint21LengthFieldPrepender())
                .addLast("outbound_config", new PacketEncoder<>(InternalGameProtocols.CLIENTBOUND));
        //Conexión
        Connection connection = new ProxyWorkerConnection(PacketFlow.SERVERBOUND);
        //Añadir a la lista
        listener.getConnections().add(connection);
        //Añadir la pipeline
        connection.configurePacketHandler(pipeline);
        //Añadir el listener
        ((ConnectionAccessor)connection).setPacketListener(new WorkerListenerImpl(server,connection));
    }

}
