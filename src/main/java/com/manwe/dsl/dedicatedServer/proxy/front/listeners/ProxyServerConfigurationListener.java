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

    private final GameProfile localGameProfile; //Read only
    private final ClientInformation localClientInformation; //Read only

    public ProxyServerConfigurationListener(MinecraftServer pServer, Connection pConnection, CommonListenerCookie pCookie) {
        super(pServer, pConnection, pCookie);
        localGameProfile = pCookie.gameProfile();
        localClientInformation = pCookie.clientInformation();
        System.out.println("ProxyServerConfigurationListener Created");
    }

    @Override
    public void handleConfigurationFinished(ServerboundFinishConfigurationPacket pPacket){
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.server);
        this.finishCurrentTask(JoinWorldTask.TYPE);
        this.connection.setupOutboundProtocol(GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(this.server.registryAccess(), this.connectionType)));
        // Packets can only be sent after the outbound protocol is set up again
        if (this.connectionType == net.neoforged.neoforge.network.connection.ConnectionType.OTHER) {
            //We need to also initialize this here, as the client may have sent the packet before we have finished our configuration.
            net.neoforged.neoforge.network.registration.NetworkRegistry.initializeNeoForgeConnection(this, java.util.Map.of());
        }
        net.neoforged.neoforge.network.registration.NetworkRegistry.onConfigurationFinished(this);

        try {
            //this.server es un ProxyDedicatedServer
            PlayerList playerlist = this.server.getPlayerList();
            if (playerlist.getPlayer(this.localGameProfile.getId()) != null) {
                this.disconnect(PlayerList.DUPLICATE_LOGIN_DISCONNECT_MESSAGE);
                return;
            }

            Component component = playerlist.canPlayerLogin(this.connection.getRemoteAddress(), this.localGameProfile);
            if (component != null) {
                this.disconnect(component);
                return;
            }

            ServerPlayer serverplayer = playerlist.getPlayerForLogin(localGameProfile, localClientInformation);
            playerlist.placeNewPlayer(this.connection, serverplayer, this.createCookie(localClientInformation, this.connectionType));

        } catch (Exception exception) {
            DistributedServerLevels.LOGGER.error("Couldn't place player in world", exception);
            this.connection.send(new ClientboundDisconnectPacket(DISCONNECT_REASON_INVALID_DATA));
            this.connection.disconnect(DISCONNECT_REASON_INVALID_DATA);
        }
    }
}
