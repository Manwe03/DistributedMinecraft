package com.manwe.dsl.mixin.accessors;

import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public interface PlayerAccessor {
    @Accessor("abilities")
    Abilities getAbilities();
}
