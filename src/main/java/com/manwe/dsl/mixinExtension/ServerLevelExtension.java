package com.manwe.dsl.mixinExtension;

import net.minecraft.world.entity.Entity;

public interface ServerLevelExtension {

    void distributedServerLevels$addEntityWithoutEvent(Entity entity);
}
