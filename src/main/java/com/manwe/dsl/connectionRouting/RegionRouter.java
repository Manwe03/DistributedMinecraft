package com.manwe.dsl.connectionRouting;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.arbiter.ConnectionInfo;
import com.manwe.dsl.config.DSLServerConfigs;
import com.manwe.dsl.dedicatedServer.proxy.ProxyDedicatedServer;
import com.manwe.dsl.dedicatedServer.proxy.WorkerTunnel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.phys.Vec2;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RegionRouter {

    /**
     * Holds assigned worker for each player
     * Holds tunnels to each worker (inbound)
     * Holds connection to each client (outbound)
     */

    //TODO modificar en el momento que se solicite un transfer
    //UUID,Tunnel for each client
    private final Map<UUID,WorkerTunnel> playerWorkerTunnels = new HashMap<>();
    //ID,Tunnel for each worker
    private final Map<Integer,WorkerTunnel> workerTunnels = new HashMap<>();
    //UUID, Connection for each client
    private final Map<UUID,Connection> playerOutboundConnections = new HashMap<>();
    private final EventLoopGroup ioGroup;

    //Topology information
    private static final int nWorkers = DSLServerConfigs.WORKER_SIZE.get();
    private static final int regionSize = DSLServerConfigs.REGION_SIZE.get();
    private static final int workerId = DSLServerConfigs.WORKER_ID.get();

    public RegionRouter(ProxyDedicatedServer server){
        this.ioGroup = new NioEventLoopGroup(1);  //TODO especificar numero correcto de hilos

        List<ConnectionInfo> workers = server.getWorkers();

        //Create a tunnel for each worker
        for(ConnectionInfo connection : workers){
            WorkerTunnel tunnel = new WorkerTunnel(new InetSocketAddress(connection.ip(),connection.port()),this,server);
            workerTunnels.put(connection.id(),tunnel);
        }
    }

    /**
     * @param playerID current player id of the incoming packets
     * @param proxyServer reference of the proxyServer
     * @return tunnel to the corresponding worker
     */
    public WorkerTunnel route(UUID playerID){
        //Return the current tunnel for this player
        WorkerTunnel tunnel = playerWorkerTunnels.get(playerID);
        if(tunnel != null) return tunnel;
        else throw new RuntimeException("Player has no tunnel to any worker");
    }

    /**
     * Transfers this player to the specified worker
     * @param playerId UUID of the player to transfer
     * @param workerId ID of the worker the player is being transferred
     * @return tunnel to the worker with workerId
     */
    public WorkerTunnel transferClientToWorker(UUID playerId, int workerId){
        WorkerTunnel newTunnel = workerTunnels.get(workerId);
        playerWorkerTunnels.put(playerId,newTunnel);//Set this player to this tunnel. All route() operations now point to this tunnel
        return newTunnel;
    }

    /**
     * @param playerId
     * @return If this player has a tunnel with some worker already registered
     */
    public boolean hasTunnel(UUID playerId){
        return playerWorkerTunnels.containsKey(playerId);
    }

    /**
     * Method for handling packets bound to a position not to the client ej: (ServerboundPlayerActionPacket)
     * @param x position in block coordinates
     * @param z position in block coordinates
     * @return Tunnel to the worker that handles this position
     */
    public WorkerTunnel route(int x, int z){
        return workerTunnels.get(computeWorkerId(x,z,DSLServerConfigs.WORKER_SIZE.get(),DSLServerConfigs.REGION_SIZE.get()));
    }

    /**
     * Routes the message to the worker with this id. No side ejects
     * @return tunnel
     */
    public WorkerTunnel route(int workerID){
        return workerTunnels.get(workerID);
    }

    public void addOutgoingConnection(UUID playerID, Connection connection){
        playerOutboundConnections.put(playerID,connection);
    }

    public Connection getOutgoingConnection(UUID playerID){
        return playerOutboundConnections.get(playerID);
    }

    public void broadCast(Packet<?> packet){
        workerTunnels.values().forEach(workerTunnel -> {
            workerTunnel.send(packet);
        });
    }

    public void returnToClient(UUID playerID, Packet<?> packet){
        Connection conn =  playerOutboundConnections.get(playerID);
        if(conn == null) {
            DistributedServerLevels.LOGGER.warn("ServerPlayer with UUID "+playerID+" does not have a Client <-> Proxy connection.");
        } else {
            conn.send(packet);
        }
    }

    public EventLoopGroup getEventLoopGroup(){
        return ioGroup;
    }

    public Map<Integer,WorkerTunnel> getWorkerTunnels(){
        return this.workerTunnels;
    }

    public static int computeWorkerId(double x, double z, int nWorkers, int regionSize){
        return computeWorkerId((int) Math.floor(x),(int) Math.floor(z),nWorkers,regionSize);
    }

    /**
     * @param x block coordinates
     * @param z block coordinates
     * @return The ID of the server allocated to this position
     */
    public static int computeWorkerId(int x, int z, int nWorkers, int regionSize){
        if(nWorkers == 1) return 1;
        if (nWorkers == 2) return z >= 0 ? 1 : 2;
        if (nWorkers % 4 != 0) throw new RuntimeException("Invalid number of workers n:"+nWorkers+". Valid numbers are 1, 2 or any other number divisible by 4");

        //To file region cords 512*512
        int regionX = x >> 9;
        int regionZ = z >> 9;

        int nWorkersCuad = nWorkers/4;
        int quad = (regionX >= 0 ? 0 : 2) + (regionZ < 0 ? 1 : 0);
        int base = quad * nWorkersCuad; //base id of the quad

        //Longest cord in abs
        int maxSide = Math.max(Math.abs(regionX),Math.abs(regionZ));

        int nRegions = maxSide / regionSize;
        int offset = (nRegions == 0) ? 1 : (int) Math.floor(Math.log(nRegions) / Math.log(2)) + 2;

        return base + offset;
    }

    /**
     * @param x chunk coordinates
     * @param z chunk coordinates
     * @return The ID of the server allocated to this position
     */
    public static boolean isChunkInWorkerDomain(int x, int z){
        if(nWorkers == 1) return true;
        if (nWorkers == 2) return z >= 0 ? workerId == 1 : workerId == 2;
        if (nWorkers % 4 != 0) throw new RuntimeException("Invalid number of workers n:"+nWorkers+". Valid numbers are 1, 2 or any other number divisible by 4");

        //To file region cords 512*512
        int regionX = x >> 5;
        int regionZ = z >> 5;

        int nWorkersCuad = nWorkers/4;
        int quad = (regionX >= 0 ? 0 : 2) + (regionZ < 0 ? 1 : 0);
        int base = quad * nWorkersCuad; //base id of the quad

        //Longest cord in abs
        int maxSide = Math.max(Math.abs(regionX),Math.abs(regionZ));

        int nRegions = maxSide / regionSize;
        int offset = (nRegions == 0) ? 1 : (int) Math.floor(Math.log(nRegions) / Math.log(2)) + 2;

        return workerId == base + offset;
    }

    /**
     * @param pChunkPos long format of chunk coordinates
     * @return The ID of the server allocated to this position
     */
    public static boolean isChunkInWorkerDomain(long pChunkPos){
        //To file region cords 512*512
        int x = ((int) (pChunkPos)) >> 5;
        int z = ((int) (pChunkPos >>> 32)) >> 5;

        if(nWorkers == 1) return true;
        if (nWorkers == 2) return z >= 0 ? workerId == 1 : workerId == 2;
        if (nWorkers % 4 != 0) throw new RuntimeException("Invalid number of workers n:"+nWorkers+". Valid numbers are 1, 2 or any other number divisible by 4");

        int nWorkersCuad = nWorkers/4;
        int quad = (x >= 0 ? 0 : 2) + (z < 0 ? 1 : 0);
        int base = quad * nWorkersCuad; //base id of the quad

        //Longest cord in abs
        int maxSide = Math.max(Math.abs(x),Math.abs(z));

        int nRegions = maxSide / regionSize;
        int offset = (nRegions == 0) ? 1 : (int) Math.floor(Math.log(nRegions) / Math.log(2)) + 2;

        return workerId == base + offset;
    }

    /**
     * @return The ID of the server that manages the spawn area
     */
    public static int defaultSpawnWorkerId(MinecraftServer server, int nWorkers, int regionSize){
        BlockPos pos = server.overworld().getSharedSpawnPos();
        return computeWorkerId(pos.getX(),pos.getZ(),nWorkers,regionSize);
    }
}
