package com.manwe.dsl.dedicatedServer.worker.listeners;

import com.manwe.dsl.mixin.accessors.ServerGamePacketListenerImplAccessor;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.item.ItemStack;

public class WorkerGamePacketListenerImpl extends ServerGamePacketListenerImpl {

    public WorkerGamePacketListenerImpl(MinecraftServer pServer, Connection pConnection, ServerPlayer pPlayer, CommonListenerCookie pCookie) {
        super(pServer, pConnection, pPlayer, pCookie);
    }

    @Override
    public void handlePlayerCommand(ServerboundPlayerCommandPacket pPacket) {
        System.out.println("ServerboundPlayerCommandPacket: " + pPacket.getAction().name());
        if(pPacket.getAction() == ServerboundPlayerCommandPacket.Action.START_FALL_FLYING){
            System.out.println("tryToStartFallFlying: "+ (!player.onGround() && !player.isFallFlying() && !player.isInWater() && !player.hasEffect(MobEffects.LEVITATION)) );
            System.out.println("onGround: "+ player.onGround());
            System.out.println("isFallFlying: "+ player.isFallFlying());
            System.out.println("isInWater: "+ player.isInWater());
            System.out.println("has Levitation: "+ player.hasEffect(MobEffects.LEVITATION));

            ItemStack itemstack = player.getItemBySlot(EquipmentSlot.CHEST);
            System.out.println("equipped item: "+itemstack.getItem());
            if (itemstack.canElytraFly(player)) {
                System.out.println("START FLYING");
            }
        }
        super.handlePlayerCommand(pPacket);
    }

    @Override
    public void handleClientInformation(ServerboundClientInformationPacket pPacket) {
        System.out.println("recibe handleClientInformation");
        super.handleClientInformation(pPacket);
    }

    @Override
    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket pPacket) {
        System.out.println("ACK -> Packet Id:" + pPacket.getId() + " AwaitingTeleport: " + ((ServerGamePacketListenerImplAccessor)this).getAwaitingTeleport() + " AwaitingPos: " + ((ServerGamePacketListenerImplAccessor)this).getAwaitingPositionFromClient());
        super.handleAcceptTeleportPacket(pPacket);
        System.out.println("Post ACK -> Packet Id:" + pPacket.getId() + " AwaitingTeleport: " + ((ServerGamePacketListenerImplAccessor)this).getAwaitingTeleport() + " AwaitingPos: " + ((ServerGamePacketListenerImplAccessor)this).getAwaitingPositionFromClient());
    }

    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket pPacket) {
        //System.out.println("handleMovePlayer: "+pPacket.getX(0)+":"+pPacket.getY(0)+":"+pPacket.getZ(0) + "AwaitingTeleport: " + ((ServerGamePacketListenerImplAccessor)this).getAwaitingTeleport() + " AwaitingPos: " + ((ServerGamePacketListenerImplAccessor)this).getAwaitingPositionFromClient());
        //System.out.println(player.getX() + ":" + player.getY() + ":" + player.getZ() + " tickCount=" + player.tickCount + " isAlive=" + player.isAlive() + " isSleeping=" + player.isSleeping() + " noPhysics=" + player.noPhysics);
        super.handleMovePlayer(pPacket);
    }

    @Override
    public void handleKeepAlive(ServerboundKeepAlivePacket pPacket) {
        super.handleKeepAlive(pPacket);
        System.out.println("(NO debería) keep alive recibido en el worker");
    }

    @Override
    public void handleChunkBatchReceived(ServerboundChunkBatchReceivedPacket pPacket) {
        //System.out.println("handleChunkBatchReceived");
        super.handleChunkBatchReceived(pPacket);
    }

    @Override
    protected void keepConnectionAlive() {
        //Desactiva el keep alive del worker, esto lo maneja solo el proxy
        //super.keepConnectionAlive();
    }
}
