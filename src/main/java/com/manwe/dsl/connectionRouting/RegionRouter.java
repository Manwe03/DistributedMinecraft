package com.manwe.dsl.connectionRouting;

import java.net.InetSocketAddress;
import java.util.List;

public class RegionRouter {

    private int port;
    private boolean proxy;
    private List<InetSocketAddress> workerList;

    public RegionRouter(){} //Initialization is deferred

    public void iniRouter(int port, boolean proxy, List<InetSocketAddress> workerList) {
        this.port = port;
        this.proxy = proxy;
        this.workerList = workerList;
    }

    public InetSocketAddress route(double x, double z){
        //Debera devolver el worker correcto dependiendo de la posición
        return workerList.get(0);
    }

    public int getPort() {
        return port;
    }
    public boolean isProxy() {
        return proxy;
    }
    public List<InetSocketAddress> getWorkerList(){
        return this.workerList;
    }
}
