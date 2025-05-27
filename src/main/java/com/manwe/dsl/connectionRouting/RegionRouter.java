package com.manwe.dsl.connectionRouting;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.dedicatedServer.proxy.ProxyDedicatedServer;
import com.manwe.dsl.dedicatedServer.proxy.WorkerTunnel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegionRouter {

    private final Map<InetSocketAddress,WorkerTunnel> workerTunnelMap = new HashMap<>();
    private final Map<UUID,Connection> playerConnections = new HashMap<>();
    private final EventLoopGroup ioGroup;

    public RegionRouter(ProxyDedicatedServer server){
        this.ioGroup = new NioEventLoopGroup(1);  //TODO especificar numero correcto de hilos

        for (InetSocketAddress address : server.getWorkers()){
            WorkerTunnel tunnel = new WorkerTunnel(address,this);
            workerTunnelMap.put(address,tunnel);
        }
    }

    public WorkerTunnel route(double x, double z){
        //Debera devolver el worker correcto dependiendo de la posición
        //De momento el primero
        return workerTunnelMap.values().stream().findFirst().orElseThrow();
    }

    public void addOutgoingConnection(UUID playerID, Connection connection){
        playerConnections.put(playerID,connection);
    }

    public Connection getOutgoingConnection(UUID playerID){
        return playerConnections.get(playerID);
    }

    public void broadCast(Packet<?> packet){
        workerTunnelMap.values().forEach(workerTunnel -> {
            workerTunnel.send(packet);
        });
    }

    public void returnToClient(UUID playerID, Packet<?> packet){
        Connection conn =  playerConnections.get(playerID);
        if(conn == null) {
            DistributedServerLevels.LOGGER.warn("ServerPlayer with UUID "+playerID+" does not have a Client <-> Proxy connection.");
        } else {
            //packetList.forEach(conn::send);
            conn.send(packet);
        }
    }

    public EventLoopGroup getEventLoopGroup(){
        return ioGroup;
    }

    public Map<InetSocketAddress,WorkerTunnel> getWorkerMap(){
        return this.workerTunnelMap;
    }
}
