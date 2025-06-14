package com.manwe.dsl.dedicatedServer.worker.listeners;

import com.manwe.dsl.dedicatedServer.worker.LocalPlayerList;
import com.manwe.dsl.mixin.accessors.ServerGamePacketListenerImplAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class WorkerFakePlayerListenerImpl extends ServerGamePacketListenerImpl {

    private final WorkerListenerImpl workerListener;

    public WorkerFakePlayerListenerImpl(MinecraftServer pServer, Connection pConnection, ServerPlayer pPlayer, CommonListenerCookie pCookie, WorkerListenerImpl workerListener) {
        super(pServer, pConnection, pPlayer, pCookie);
        this.workerListener = workerListener;
    }

    /**
     * Disconnect the fake player silently, (only in server no change should be sent to client)
     * @param pDetails ignored
     */
    @Override
    public void onDisconnect(DisconnectionDetails pDetails){
        this.silentDisconnect();
    }

    public void silentDisconnect(){
        ((ServerGamePacketListenerImplAccessor)this).getChatMessageChain().close();
        this.server.invalidateStatus();
        this.server.getPlayerList().broadcastSystemMessage(Component.translatable("multiplayer.player.left", this.player.getDisplayName()).withStyle(ChatFormatting.AQUA), false);
        this.player.disconnect();
        if(!(this.server.getPlayerList() instanceof LocalPlayerList localPlayerList)) throw new RuntimeException("playerList not instance of LocalPlayerList");
        localPlayerList.silentRemoveFakePlayer(this.player);
        //this.server.getPlayerList().remove(this.player);
        this.player.getTextFilter().leave();
    }

    /*
    @Override
    public void tick() {
        //this.player.doTick();
        super.tick(); //TODO ver si se puede desactivar
    }
    */

    @Override
    protected void keepConnectionAlive() {
        //Desactiva el keep alive del worker, esto lo maneja solo el proxy
    }
}
