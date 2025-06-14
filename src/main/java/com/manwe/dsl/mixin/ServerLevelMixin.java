package com.manwe.dsl.mixin;

import com.manwe.dsl.mixinExtension.ServerLevelExtension;
import com.manwe.dsl.config.DSLServerConfigs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements ServerLevelExtension {

    //TODO puede que se utilize esto para manejar estados globales como el daytime

    @Shadow @Nonnull public abstract MinecraftServer getServer();

    @Shadow @Final private PersistentEntitySectionManager<Entity> entityManager;

    /**
     * @author Manwe
     * @reason No ticking for the ServerLevels in the proxy
     */
    @Inject(method = "tick", at=@At("HEAD"), cancellable = true)
    public void tick(BooleanSupplier pHasTimeLeft, CallbackInfo ci){
        if(DSLServerConfigs.IS_PROXY.get()) ci.cancel();
    }

    @Unique
    public void distributedServerLevels$addEntityWithoutEvent(Entity entity){
        this.entityManager.addNewEntityWithoutEvent(entity);
    }
}
