package com.manwe.dsl.mixin.accessors;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayer.class)
public interface ServerPlayerAccessor {

    @Accessor("requestedViewDistance")
    void setRequestedViewDistance(int v);
}
