package com.manwe.dsl.mixin.accessors;

import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SynchedEntityData.class)
public interface SynchedEntityDataAccessor {
    @Accessor("isDirty")
    void setDirty(boolean dirty);

    @Accessor("itemsById")
    SynchedEntityData.DataItem<?>[] getItemsById();
}
