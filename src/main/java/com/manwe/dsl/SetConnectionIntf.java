package com.manwe.dsl;

import net.minecraft.server.network.ServerConnectionListener;

public interface SetConnectionIntf {
    void setConnection(ServerConnectionListener connection);
}
