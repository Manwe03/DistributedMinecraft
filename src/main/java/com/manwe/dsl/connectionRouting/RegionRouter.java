package com.manwe.dsl.connectionRouting;

import com.manwe.dsl.dedicatedServer.proxy.ProxyDedicatedServer;
import com.manwe.dsl.dedicatedServer.proxy.WorkerTunnel;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.PlayerInitPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.ProxyWorkerPacket;
import io.netty.channel.EventLoopGroup;
import net.minecraft.network.Connection;
import net.neoforged.neoforge.network.connection.ConnectionType;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegionRouter {

    private final Connection clientConnection; //Conexión cliente - proxy, puede servir para devolver paquetes? hay que mirar esto TODO
    private final Map<InetSocketAddress,WorkerTunnel> workerTunnelMap = new HashMap<>();

    public RegionRouter(Connection clientConnection, ProxyDedicatedServer server, PlayerInitPacket initPacket, ConnectionType connectionType){
        this.clientConnection = clientConnection;
        EventLoopGroup ioGroup = clientConnection.channel().eventLoop().parent();  // reutiliza grupo

        for (InetSocketAddress address : server.getWorkers()){
            WorkerTunnel tunnel = new WorkerTunnel(address, ioGroup, server, connectionType, initPacket);
            workerTunnelMap.put(address,tunnel);

            tunnel.send(initPacket).addListener(future -> {
                if(future.isSuccess()){
                    System.out.println("PlayerPacket was sent");
                    //tunnel.setUpVannillaPlayProtocol(); //Change pipeline to vanilla play packet types
                    //System.out.println("Vanilla pipeline setup");
                }else {
                    System.out.println("PlayerPacket was not sent");
                }
            });

            //tunnel.send(new ProxyWorkerPacket(null)); //Send wrapper
        }
    }

    public WorkerTunnel route(double x, double z){
        //Debera devolver el worker correcto dependiendo de la posición
        //De momento el primero
        return workerTunnelMap.values().stream().findFirst().orElseThrow();
    }

    /**
     * Should be called when a player disconnects from the proxy
     */
    public void disconectWorkerTunnels(){
        //TODO mandar un disconnection package a todos los workers para que gestionen la desconexión
        /*
        workerTunnelMap.values().forEach(workerTunnel -> {

        });
         */
    }

    public Map<InetSocketAddress,WorkerTunnel> getWorkerMap(){
        return this.workerTunnelMap;
    }
}
