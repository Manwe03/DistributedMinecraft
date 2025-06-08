package com.manwe.dsl.mixin.accessors;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplAccessor {

    @Accessor("tickCount")
    int getTickCount();

    @Accessor("awaitingPositionFromClient")
    Vec3 getAwaitingPositionFromClient();

    @Accessor("awaitingTeleport")
    int getAwaitingTeleport();

    @Accessor("awaitingTeleport")
    void setAwaitingTeleport(int a);

    @Accessor("firstGoodX")
    double getFirstGoodX();

    @Accessor("firstGoodY")
    double getFirstGoodY();

    @Accessor("firstGoodZ")
    double getFirstGoodZ();

    @Accessor("firstGoodX")
    void setFirstGoodX(double a);

    @Accessor("firstGoodY")
    void setFirstGoodY(double a);

    @Accessor("firstGoodZ")
    void setFirstGoodZ(double a);

    @Accessor("lastGoodX")
    double getLastGoodX();

    @Accessor("lastGoodY")
    double getLastGoodY();

    @Accessor("lastGoodZ")
    double getLastGoodZ();

    @Accessor("lastGoodX")
    void setLastGoodX(double a);

    @Accessor("lastGoodY")
    void setLastGoodY(double a);

    @Accessor("lastGoodZ")
    void setLastGoodZ(double a);

    @Accessor("receivedMovePacketCount")
    int getReceivedMovePacketCount();

    @Accessor("receivedMovePacketCount")
    void setReceivedMovePacketCount(int i);

    @Accessor("knownMovePacketCount")
    int getKnownMovePacketCount();

    @Accessor("clientIsFloating")
    void setClientIsFloating(boolean a);
}
