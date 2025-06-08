package com.manwe.dsl.dedicatedServer.proxy.front.listeners;

import com.manwe.dsl.DistributedServerLevels;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.config.JoinWorldTask;
import net.minecraft.server.players.PlayerList;

/**
 * Stack de listener vanilla
 * Handshake -> Login -> Configuration -> Game
 * ProxyServerHandshakeListener -> Login -> ProxyServerConfigurationListener -> Game
 *
 * Manda a cada worker el inicio de su connexion, manda la información del ServerPlayer
 */
public class ProxyServerConfigurationListener extends ServerConfigurationPacketListenerImpl {

    private static final Component DISCONNECT_REASON_INVALID_DATA = Component.translatable("multiplayer.disconnect.invalid_player_data");

    public ProxyServerConfigurationListener(MinecraftServer pServer, Connection pConnection, CommonListenerCookie pCookie) {
        super(pServer, pConnection, pCookie);
        System.out.println("ProxyServerConfigurationListener Created");
    }


    @Override
    public void handleConfigurationFinished(ServerboundFinishConfigurationPacket pPacket){
        System.out.println("handleConfigurationFinished");
        super.handleConfigurationFinished(pPacket);
    }
}
