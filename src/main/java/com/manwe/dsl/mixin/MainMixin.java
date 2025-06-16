package com.manwe.dsl.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.manwe.dsl.dedicatedServer.CustomDedicatedServer;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.*;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.storage.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.awt.*;
import java.util.function.Function;

@Mixin(value = Main.class, remap = true)
public class MainMixin {

    @ModifyArg(
            method = "main([Ljava/lang/String;)V",
            index = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;spin(Ljava/util/function/Function;)Lnet/minecraft/server/MinecraftServer;")
    )
    private static Function<Thread, MinecraftServer> spin(Function<Thread, MinecraftServer> pThreadFunction,
                                                          @Local(name = "gametestEnabled") boolean gametestEnabled,
                                                          @Local(name = "optionset") OptionSet optionset,
                                                          @Local(name = "spawnPosOpt") OptionSpec<BlockPos> spawnPosOpt,
                                                          @Local(name = "levelstoragesource$levelstorageaccess") LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess,
                                                          @Local(name = "packrepository") PackRepository packrepository,
                                                          @Local(name = "worldstem") WorldStem worldstem,
                                                          @Local(name = "dedicatedserversettings") DedicatedServerSettings dedicatedserversettings,
                                                          @Local(name = "services") Services services,
                                                          @Local(name = "optionspec11") OptionSpec<Integer> optionspec11,
                                                          @Local(name = "optionspec2") OptionSpec<Void> optionspec2,
                                                          @Local(name = "optionspec12") OptionSpec<String> optionspec12,
                                                          @Local(name = "optionspec") OptionSpec<Void> optionspec,
                                                          @Local(name = "optionspec15") OptionSpec<String> optionspec15

    ) {
        return (threadExecutor) -> {
            CustomDedicatedServer dedicatedserver1;

            //Punto inicial de Modificación pasa un Custom Dedicated Server que inicia una conexión custom
            dedicatedserver1 = new CustomDedicatedServer(threadExecutor, levelstoragesource$levelstorageaccess, packrepository, worldstem, dedicatedserversettings, DataFixers.getDataFixer(), services, LoggerChunkProgressListener::createFromGameruleRadius);

            dedicatedserver1.setPort(optionset.valueOf(optionspec11));
            dedicatedserver1.setDemo(optionset.has(optionspec2));
            dedicatedserver1.setId(optionset.valueOf(optionspec12));
            boolean flag2 = !optionset.has(optionspec) && !optionset.valuesOf(optionspec15).contains("nogui");
            if (dedicatedserver1 instanceof CustomDedicatedServer dedicatedServer && flag2 && !GraphicsEnvironment.isHeadless()) {
                dedicatedServer.showGui();
            }

            return dedicatedserver1;
        };
    }
}
