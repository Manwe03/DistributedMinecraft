package com.manwe.dsl.dedicatedServer.proxy.front.listeners;

import com.google.common.primitives.Ints;
import com.manwe.dsl.DistributedServerLevels;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.logging.LogUtils;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.login.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyServerLoginListener implements ServerLoginPacketListener, TickablePacketListener {

    static final Component DISCONNECT_UNEXPECTED_QUERY = Component.translatable("multiplayer.disconnect.unexpected_query_response");

    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_TICKS_BEFORE_LOGIN = 600;
    private final byte[] challenge;
    final MinecraftServer server;
    final Connection connection;
    private volatile State state = State.HELLO;
    /**
     * How long has player been trying to login into the server.
     */
    private int tick;
    @Nullable
    String requestedUsername;
    @Nullable
    private GameProfile authenticatedProfile;
    private final String serverId = "";
    private final boolean transferred;

    public ProxyServerLoginListener(MinecraftServer pServer, Connection pConnection, boolean pTransferred) {
        this.server = pServer;
        this.connection = pConnection;
        this.challenge = Ints.toByteArray(RandomSource.create().nextInt());
        this.transferred = pTransferred;
        System.out.println("ProxyServerLoginListener Created");
    }

    @Override
    public void tick() {
        if (this.state == State.VERIFYING) {
            this.verifyLoginAndFinishConnectionSetup(Objects.requireNonNull(this.authenticatedProfile));
        }

        if (this.state == State.WAITING_FOR_DUPE_DISCONNECT
                && !this.isPlayerAlreadyInWorld(Objects.requireNonNull(this.authenticatedProfile))) {
            this.finishLoginAndWaitForClient(this.authenticatedProfile);
        }

        if (this.tick++ == 600) {
            this.disconnect(Component.translatable("multiplayer.disconnect.slow_login"));
        }
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    public void disconnect(Component pReason) {
        try {
            LOGGER.info("Disconnecting {}: {}", this.getUserName(), pReason.getString());
            this.connection.send(new ClientboundLoginDisconnectPacket(pReason));
            this.connection.disconnect(pReason);
        } catch (Exception exception) {
            LOGGER.error("Error whilst disconnecting player", (Throwable)exception);
        }
    }

    private boolean isPlayerAlreadyInWorld(GameProfile pProfile) {
        return this.server.getPlayerList().getPlayer(pProfile.getId()) != null;
    }

    @Override
    public void onDisconnect(DisconnectionDetails pDetails) {
        LOGGER.info("{} lost connection: {}", this.getUserName(), pDetails.reason().getString());
    }

    public String getUserName() {
        String s = this.connection.getLoggableAddress(this.server.logIPs());
        return this.requestedUsername != null ? this.requestedUsername + " (" + s + ")" : s;
    }

    @Override
    public void handleHello(ServerboundHelloPacket pPacket) {
        Validate.validState(this.state == State.HELLO, "Unexpected hello packet");
        Validate.validState(StringUtil.isValidPlayerName(pPacket.name()), "Invalid characters in username");
        this.requestedUsername = pPacket.name();
        GameProfile gameprofile = this.server.getSingleplayerProfile();
        if (gameprofile != null && this.requestedUsername.equalsIgnoreCase(gameprofile.getName())) {
            this.startClientVerification(gameprofile);
        } else {
            if (this.server.usesAuthentication() && !this.connection.isMemoryConnection()) {
                this.state = State.KEY;
                this.connection.send(new ClientboundHelloPacket("", this.server.getKeyPair().getPublic().getEncoded(), this.challenge, true));
            } else {
                this.startClientVerification(UUIDUtil.createOfflineProfile(this.requestedUsername));
            }
        }
    }

    void startClientVerification(GameProfile pAuthenticatedProfile) {
        this.authenticatedProfile = pAuthenticatedProfile;
        this.state = State.VERIFYING;
    }

    private void verifyLoginAndFinishConnectionSetup(GameProfile pProfile) {
        PlayerList playerlist = this.server.getPlayerList();
        Component component = playerlist.canPlayerLogin(this.connection.getRemoteAddress(), pProfile);
        if (component != null) {
            this.disconnect(component);
        } else {
            if (this.server.getCompressionThreshold() >= 0 && !this.connection.isMemoryConnection()) {
                this.connection
                        .send(
                                new ClientboundLoginCompressionPacket(this.server.getCompressionThreshold()),
                                PacketSendListener.thenRun(() -> this.connection.setupCompression(this.server.getCompressionThreshold(), true))
                        );
            }

            boolean flag = playerlist.disconnectAllPlayersWithProfile(pProfile);
            if (flag) {
                this.state = State.WAITING_FOR_DUPE_DISCONNECT;
            } else {
                this.finishLoginAndWaitForClient(pProfile);
            }
        }
    }

    private void finishLoginAndWaitForClient(GameProfile pProfile) {
        this.state = State.PROTOCOL_SWITCHING;
        this.connection.send(new ClientboundGameProfilePacket(pProfile, true));
    }

    @Override
    public void handleKey(ServerboundKeyPacket pPacket) {
        Validate.validState(this.state == State.KEY, "Unexpected key packet");

        final String s;
        try {
            PrivateKey privatekey = this.server.getKeyPair().getPrivate();
            if (!pPacket.isChallengeValid(this.challenge, privatekey)) {
                throw new IllegalStateException("Protocol error");
            }

            SecretKey secretkey = pPacket.getSecretKey(privatekey);
            Cipher cipher = Crypt.getCipher(2, secretkey);
            Cipher cipher1 = Crypt.getCipher(1, secretkey);
            s = new BigInteger(Crypt.digestData("", this.server.getKeyPair().getPublic(), secretkey)).toString(16);
            this.state = State.AUTHENTICATING;
            this.connection.setEncryptionKey(cipher, cipher1);
        } catch (CryptException cryptexception) {
            throw new IllegalStateException("Protocol error", cryptexception);
        }

        Thread thread = new Thread("User Authenticator #" + UNIQUE_THREAD_ID.incrementAndGet()) {
            @Override
            public void run() {
                String s1 = Objects.requireNonNull(ProxyServerLoginListener.this.requestedUsername, "Player name not initialized");

                try {
                    ProfileResult profileresult = ProxyServerLoginListener.this.server.getSessionService().hasJoinedServer(s1, s, this.getAddress());
                    if (profileresult != null) {
                        GameProfile gameprofile = profileresult.profile();
                        DistributedServerLevels.LOGGER.info("UUID of player {} is {}", gameprofile.getName(), gameprofile.getId());
                        ProxyServerLoginListener.this.startClientVerification(gameprofile);
                    } else if (ProxyServerLoginListener.this.server.isSingleplayer()) {
                        DistributedServerLevels.LOGGER.warn("Failed to verify username but will let them in anyway!");
                        ProxyServerLoginListener.this.startClientVerification(UUIDUtil.createOfflineProfile(s1));
                    } else {
                        ProxyServerLoginListener.this.disconnect(Component.translatable("multiplayer.disconnect.unverified_username"));
                        DistributedServerLevels.LOGGER.error("Username '{}' tried to join with an invalid session", s1);
                    }
                } catch (AuthenticationUnavailableException authenticationunavailableexception) {
                    if (ProxyServerLoginListener.this.server.isSingleplayer()) {
                        DistributedServerLevels.LOGGER.warn("Authentication servers are down but will let them in anyway!");
                        ProxyServerLoginListener.this.startClientVerification(UUIDUtil.createOfflineProfile(s1));
                    } else {
                        ProxyServerLoginListener.this.disconnect(Component.translatable("multiplayer.disconnect.authservers_down"));
                        DistributedServerLevels.LOGGER.error("Couldn't verify username because servers are unavailable");
                    }
                }
            }

            @Nullable
            private InetAddress getAddress() {
                SocketAddress socketaddress = ProxyServerLoginListener.this.connection.getRemoteAddress();
                return ProxyServerLoginListener.this.server.getPreventProxyConnections() && socketaddress instanceof InetSocketAddress
                        ? ((InetSocketAddress)socketaddress).getAddress()
                        : null;
            }
        };
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
    }

    @Override
    public void handleCustomQueryPacket(ServerboundCustomQueryAnswerPacket pPacket) {
        this.disconnect(DISCONNECT_UNEXPECTED_QUERY);
    }

    @Override
    public void handleLoginAcknowledgement(ServerboundLoginAcknowledgedPacket pPacket) {
        Validate.validState(this.state == State.PROTOCOL_SWITCHING, "Unexpected login acknowledgement packet");
        this.connection.setupOutboundProtocol(ConfigurationProtocols.CLIENTBOUND);
        CommonListenerCookie commonlistenercookie = CommonListenerCookie.createInitial(Objects.requireNonNull(this.authenticatedProfile), this.transferred);
        ServerConfigurationPacketListenerImpl serverconfigurationpacketlistenerimpl = new ProxyServerConfigurationListener(
                this.server, this.connection, commonlistenercookie
        );
        this.connection.setupInboundProtocol(ConfigurationProtocols.SERVERBOUND, serverconfigurationpacketlistenerimpl);
        serverconfigurationpacketlistenerimpl.startConfiguration();
        this.state = State.ACCEPTED;
    }

    @Override
    public void fillListenerSpecificCrashDetails(CrashReport pCrashReport, CrashReportCategory pCategory) {
        pCategory.setDetail("Login phase", () -> this.state.toString());
    }

    @Override
    public void handleCookieResponse(ServerboundCookieResponsePacket pPacket) {
        this.disconnect(DISCONNECT_UNEXPECTED_QUERY);
    }

    static enum State {
        HELLO,
        KEY,
        AUTHENTICATING,
        NEGOTIATING,
        VERIFYING,
        WAITING_FOR_DUPE_DISCONNECT,
        PROTOCOL_SWITCHING,
        ACCEPTED;
    }
}
