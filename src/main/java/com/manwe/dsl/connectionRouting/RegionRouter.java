package com.manwe.dsl.connectionRouting;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.arbiter.ConnectionInfo;
import com.manwe.dsl.config.DSLServerConfigs;
import com.manwe.dsl.dedicatedServer.CustomDedicatedServer;
import com.manwe.dsl.dedicatedServer.proxy.WorkerTunnel;
import com.manwe.dsl.dedicatedServer.worker.packets.login.WorkerBoundRequestLevelInformationPacket;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;

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
    private BlockPos defaultSpawnPos;

    //Topology information
    private static final int nWorkers = DSLServerConfigs.WORKER_SIZE.get();
    private static final int regionSize = DSLServerConfigs.REGION_SIZE.get();
    private static final int workerId = DSLServerConfigs.WORKER_ID.get();

    public final CustomDedicatedServer server;

    public RegionRouter(CustomDedicatedServer server){
        this.ioGroup = new NioEventLoopGroup(1);  //TODO especificar numero correcto de hilos
        this.server = server;

        List<ConnectionInfo> workers = this.server.getWorkers();
        //Create a tunnel for each worker
        for(ConnectionInfo connection : workers){
            WorkerTunnel tunnel = new WorkerTunnel(new InetSocketAddress(connection.ip(),connection.port()),this, this.server);
            workerTunnels.put(connection.id(),tunnel);
        }
        workerTunnels.get(1).send(new WorkerBoundRequestLevelInformationPacket()); //Request the spawn position
    }

    /**
     * @param playerID current player id of the incoming packets
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
     */
    public void transferClientToWorker(UUID playerId, int workerId){
        WorkerTunnel newTunnel = workerTunnels.get(workerId);
        playerWorkerTunnels.put(playerId,newTunnel); //Set this player to this tunnel. All route() operations now point to this tunnel
    }

    /**
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
        return workerTunnels.get(computeWorkerId(x,z));
    }

    /**
     * Routes the message to the worker with this id. No side ejects
     * @return tunnel
     */
    public WorkerTunnel route(int workerID){
        //System.out.println("ROUTE -> "+workerID);
        return workerTunnels.get(workerID);
    }

    public void addOutgoingConnection(UUID playerID, Connection connection){
        playerOutboundConnections.put(playerID,connection);
    }

    public Connection getOutgoingConnection(UUID playerID){
        return playerOutboundConnections.get(playerID);
    }

    public void broadCast(Packet<?> packet){
        workerTunnels.values().forEach(workerTunnel -> workerTunnel.send(packet));
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

    /**
     * @param x block coordinates
     * @param z block coordinates
     * @return The ID of the server allocated to this position
     */
    public static int computeWorkerId(double x, double z){
        return computeWorkerId((int) Math.floor(x), (int) Math.floor(z));
    }

    /**
     * @param blockX block coordinates
     * @param blockZ block coordinates
     * @return The ID of the server allocated to this position
     */
    public static int computeWorkerId(int blockX, int blockZ){
        return computeWorkerIdChunk(blockX >> 4,blockZ >> 4);
    }

    /**
     * @param chunkX chunk coordinates
     * @param chunkZ chunk coordinates
     * @return The ID of the server allocated to this position
     */
    private static int computeWorkerIdChunk(int chunkX, int chunkZ) {
        int regionX = chunkX >> regionSize;
        int regionZ = chunkZ >> regionSize;
        return Math.min(Math.max((regionX < 0 ? (-regionX) - 1 : regionX), (regionZ < 0 ? (-regionZ) - 1 : regionZ)) + 1, nWorkers);
    }

    /**
     * @param chunkX chunk coordinates
     * @param chunkZ chunk coordinates
     * @return The ID of the server allocated to this position
     */
    public static boolean isChunkOutsideWorkerDomain(int chunkX, int chunkZ){
        return workerId != computeWorkerIdChunk(chunkX,chunkZ);
    }

    /**
     * @param pChunkPos long format of chunk coordinates
     * @return The ID of the server allocated to this position
     */
    public static boolean isChunkOutsideWorkerDomain(long pChunkPos){
        //To file region cords 512*512
        int chunkX = (int) (pChunkPos);
        int chunkZ = (int) (pChunkPos >>> 32);
        return isChunkOutsideWorkerDomain(chunkX,chunkZ);
    }

    public void setDefaultSpawn(BlockPos pos){
        this.defaultSpawnPos = pos;
    }

    /**
     * @return The ID of the server that manages the spawn area
     */
    public int defaultSpawnWorkerId(){
        if(defaultSpawnPos == null) throw new RuntimeException("No defaultSpawnPos set");
        return computeWorkerId(defaultSpawnPos.getX(),defaultSpawnPos.getZ());
    }
}
