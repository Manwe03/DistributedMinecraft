package com.manwe.dsl.dedicatedServer.proxy;

import com.manwe.dsl.dedicatedServer.proxy.back.packets.PlayerInitPacket;
import com.manwe.dsl.dedicatedServer.InternalGameProtocols;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.flow.FlowControlHandler;
import net.minecraft.network.*;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.network.connection.ConnectionType;

import java.net.InetSocketAddress;

/**
 * Cliente del Proxy hacia los workers
 */
public class WorkerTunnel {
    private final InetSocketAddress workerAddress; // dirección al worker
    private final Connection connection;
    private final Channel channel;

    public WorkerTunnel(InetSocketAddress workerAddress, EventLoopGroup workerGroup, MinecraftServer server, ConnectionType connectionType, PlayerInitPacket initPacket) {        // para mandar C→S←W
        System.out.println("WorkerTunnel Iniciado Cliente: Proxy -> Worker");
        this.workerAddress = workerAddress;

        this.connection = new  Connection(PacketFlow.CLIENTBOUND);
        this.channel = new Bootstrap() // Inicialización del cliente
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<>() {
                    @Override protected void initChannel(Channel ch) {
                        ChannelPipeline pipeline = ch.pipeline();

                        // Añadir splitters y handlers necesarios
                        // pipeline.addLast("splitter", new Varint21FrameDecoder(null)); //Si nos llegara algo a este cliente
                        pipeline.addLast(new FlowControlHandler());

                        // Aquí se pasa el protocolo custom
                        // pipeline.addLast("decoder", new PacketDecoder<>(WorkerHandshakeProtocols.CLIENTBOUND)); //Si nos llegara algo a este cliente
                        pipeline.addLast("prepender", new Varint21LengthFieldPrepender());
                        pipeline.addLast("encoder", new PacketEncoder<>(InternalGameProtocols.SERVERBOUND));

                        connection.configurePacketHandler(pipeline);

                    }
                })
                .connect(workerAddress)
                .syncUninterruptibly()
                .channel();
    }

    /**
     * Manda como cliente los paquetes al worker
     */
    public ChannelFuture send(net.minecraft.network.protocol.Packet<?> pkt) {
        return this.connection.channel().writeAndFlush(pkt);
    }

    public void sendRaw(ByteBuf buf) { this.connection.channel().writeAndFlush(buf); }

    /**
     * Modifica la conexión para manejar el envío de paquetes "PLAY" vanilla
     */
    public void setUpVannillaPlayProtocol(){
        //Eliminar la configuración
        connection.channel().pipeline().remove("prepender");
        connection.channel().pipeline().remove("encoder");
        connection.channel().pipeline().remove("hackfix");
        connection.channel().pipeline().remove("packet_handler");

        //Configuración vanilla
        Connection.configureSerialization(connection.channel().pipeline(), PacketFlow.CLIENTBOUND, false, null);
        //connection.configurePacketHandler(connection.channel().pipeline());
    }

    public void sendDisconect(){
        //TODO debería intentar mandar un paquete con la infraestructura de NeoForge
    }

    /**
     * Address of the worker, the endpoint of this tunnel
     */
    public InetSocketAddress tunnelEndAddress() { return workerAddress; }
}
