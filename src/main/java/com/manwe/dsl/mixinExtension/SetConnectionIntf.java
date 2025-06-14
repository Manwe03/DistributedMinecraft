package com.manwe.dsl.mixinExtension;

import net.minecraft.server.network.ServerConnectionListener;

public interface SetConnectionIntf {
    void setConnection(ServerConnectionListener connection);
}
