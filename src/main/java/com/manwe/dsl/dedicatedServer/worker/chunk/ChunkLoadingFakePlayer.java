package com.manwe.dsl.dedicatedServer.worker.chunk;

import com.manwe.dsl.dedicatedServer.worker.packets.chunkloading.WorkerBoundFakePlayerLoginPacket;
import com.manwe.dsl.mixin.accessors.ServerPlayerAccessor;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.*;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stat;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Consumer;

public class ChunkLoadingFakePlayer extends ServerPlayer {

    public ChunkLoadingFakePlayer(MinecraftServer server, WorkerBoundFakePlayerLoginPacket packet) {
        super(server, server.getLevel(packet.levelResourcekey) == null ? server.overworld() : server.getLevel(packet.levelResourcekey) , packet.gameprofile, ClientInformation.createDefault());
        this.absMoveTo(packet.pos.x, packet.pos.y, packet.pos.z);
        setFakePlayerRequestedViewDistance(packet.viewDistance);
    }

    public void setFakePlayerRequestedViewDistance(int viewDistance) {
        ((ServerPlayerAccessor)this).setRequestedViewDistance(viewDistance); //Set view distance as this player has no ClientInformation
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void displayClientMessage(Component chatComponent, boolean actionBar) {}

    @Override
    public void awardStat(Stat<?> stat, int amount) {}

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean canHarmPlayer(Player player) {
        return false;
    }

    @Override
    public void die(DamageSource source) {}

    /*
    @Override
    public void tick() {
        //super.tick();

        Entity entity = this.getCamera();
        if(entity == this) return;
        this.serverLevel().getChunkSource().move(this);
    }

    @Override
    public void doTick() {
        super.doTick();
        //this.tick();
    }
    */

    @Override
    public void updateOptions(ClientInformation p_301998_) {}

    @Override
    public OptionalInt openMenu(@Nullable MenuProvider p_9033_, @Nullable Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        return OptionalInt.empty();
    }

    @Override
    public void openHorseInventory(AbstractHorse horse, Container container) {}

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        return false;
    }

    @Override
    @Nullable
    public MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public boolean isFakePlayer() {
        return true;
    }

}