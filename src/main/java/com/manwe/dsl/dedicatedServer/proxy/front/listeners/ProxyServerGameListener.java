package com.manwe.dsl.dedicatedServer.proxy.front.listeners;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.connectionRouting.RegionRouter;
import com.manwe.dsl.dedicatedServer.proxy.WorkerTunnel;
import com.manwe.dsl.dedicatedServer.proxy.ProxyDedicatedServer;
import com.manwe.dsl.dedicatedServer.worker.packets.WorkerBoundPlayerDisconnectPacket;
import com.manwe.dsl.dedicatedServer.worker.packets.WorkerBoundContainerPacket;
import io.netty.channel.ChannelFuture;
import net.minecraft.Util;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.*;
import org.jetbrains.annotations.NotNull;

public class ProxyServerGameListener extends ServerGamePacketListenerImpl {

    //Direct pre-casted reference
    ProxyDedicatedServer server;
    RegionRouter router;

    //boolean blockClientMovementBeforeACK = true;
    public ProxyServerGameListener(MinecraftServer pServer, Connection pConnection, ServerPlayer pPlayer, CommonListenerCookie pCookie, RegionRouter router) {
        super(pServer, pConnection, pPlayer, pCookie);
        if(pServer instanceof ProxyDedicatedServer server1){
            this.server = server1;
            this.router = router;
        }else {
            throw new RuntimeException("ProxyServerGameListener not initialized from a ProxyDedicatedServer");
        }
    }

    ////////////////////////////////////
    ///ServerCommonPacketListenerImpl///
    ////////////////////////////////////

    @Override
    public void handleKeepAlive(ServerboundKeepAlivePacket pPacket) {
        super.handleKeepAlive(pPacket);
        //Manejado en el proxy
        //System.out.println("Keep alive manejado en el proxy");
    }

    //////////////////////////////////
    ///ServerGamePacketListenerImpl///
    //////////////////////////////////

    @Override
    public void tick() {

        this.keepConnectionAlive(); //Only tick keepConnectionAlive

        //Cancel all ticking for this ServerPlayer in the proxy, ticking is done by the workers
    }

    /**
     * Processes player movement input. Includes walking, strafing, jumping, and sneaking. Excludes riding and toggling flying/sprinting.
     */
    @Override
    public void handlePlayerInput(@NotNull ServerboundPlayerInputPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleMoveVehicle(@NotNull ServerboundMoveVehiclePacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleAcceptTeleportPacket(@NotNull ServerboundAcceptTeleportationPacket pPacket) {
        //blockClientMovementBeforeACK = false;
        //System.out.println("ACK desbloqueado movement");

        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        ChannelFuture future = tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleRecipeBookSeenRecipePacket(@NotNull ServerboundRecipeBookSeenRecipePacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleRecipeBookChangeSettingsPacket(@NotNull ServerboundRecipeBookChangeSettingsPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleSeenAdvancements(@NotNull ServerboundSeenAdvancementsPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    /**
     * This method is only called for manual tab-completion (the {@link net.minecraft.commands.synchronization.SuggestionProviders#ASK_SERVER minecraft:ask_server} suggestion provider).
     */
    @Override
    public void handleCustomCommandSuggestions(@NotNull ServerboundCommandSuggestionPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleSetCommandBlock(@NotNull ServerboundSetCommandBlockPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleSetCommandMinecart(@NotNull ServerboundSetCommandMinecartPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handlePickItem(@NotNull ServerboundPickItemPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleRenameItem(@NotNull ServerboundRenameItemPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleSetBeaconPacket(@NotNull ServerboundSetBeaconPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleSetStructureBlock(@NotNull ServerboundSetStructureBlockPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleSetJigsawBlock(@NotNull ServerboundSetJigsawBlockPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleJigsawGenerate(@NotNull ServerboundJigsawGeneratePacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleSelectTrade(@NotNull ServerboundSelectTradePacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleEditBook(@NotNull ServerboundEditBookPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleEntityTagQuery(@NotNull ServerboundEntityTagQueryPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleContainerSlotStateChanged(@NotNull ServerboundContainerSlotStateChangedPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleBlockEntityTagQuery(@NotNull ServerboundBlockEntityTagQueryPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleMovePlayer(@NotNull ServerboundMovePlayerPacket pPacket) {
        //System.out.println("Send Movement");
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(), pPacket)); //Send wrapped movement packet
    }

    /**
     * Processes the player initiating/stopping digging on a particular spot, as well as a player dropping items
     */
    @Override
    public void handlePlayerAction(@NotNull ServerboundPlayerActionPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleUseItemOn(@NotNull ServerboundUseItemOnPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    /**
     * Called when a client is using an item while not pointing at a block, but simply using an item
     */
    @Override
    public void handleUseItem(@NotNull ServerboundUseItemPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleTeleportToEntityPacket(@NotNull ServerboundTeleportToEntityPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handlePaddleBoat(@NotNull ServerboundPaddleBoatPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void onDisconnect(DisconnectionDetails pDetails) {
        DistributedServerLevels.LOGGER.info("{} lost connection: {}", this.player.getName().getString(), pDetails.reason().getString());
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundPlayerDisconnectPacket(this.player.getUUID()));
    }

    /**
     * Updates which quickbar slot is selected
     */
    @Override
    public void handleSetCarriedItem(@NotNull ServerboundSetCarriedItemPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    /**
     * Process chat messages (broadcast back to clients) and commands (executes)
     */
    @Override //TODO como manejar el chat?
    public void handleChat(@NotNull ServerboundChatPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleChatCommand(@NotNull ServerboundChatCommandPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleSignedChatCommand(@NotNull ServerboundChatCommandSignedPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleChatAck(@NotNull ServerboundChatAckPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleAnimate(@NotNull ServerboundSwingPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    /**
     * Processes a range of action-types: sneaking, sprinting, waking from sleep, opening the inventory or setting jump height of the horse the player is riding
     */
    @Override
    public void handlePlayerCommand(@NotNull ServerboundPlayerCommandPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override//TODO como manejar el ping?
    public void handlePingRequest(@NotNull ServerboundPingRequestPacket pPacket) {
        super.handlePingRequest(pPacket);
    }

    /**
     * Processes left and right clicks on entities
     */
    @Override
    public void handleInteract(@NotNull ServerboundInteractPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    /**
     * Processes the client status updates: respawn attempt from player, opening statistics or achievements, or acquiring 'open inventory' achievement
     */
    @Override
    public void handleClientCommand(@NotNull ServerboundClientCommandPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    /**
     * Processes the client closing windows (container)
     */
    @Override
    public void handleContainerClose(@NotNull ServerboundContainerClosePacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    /**
     * Executes a container/inventory slot manipulation as indicated by the packet. Sends the serverside result if they didn't match the indicated result and prevents further manipulation by the player until he confirms that it has the same open container/inventory
     */
    @Override
    public void handleContainerClick(@NotNull ServerboundContainerClickPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handlePlaceRecipe(@NotNull ServerboundPlaceRecipePacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    /**
     * Enchants the item identified by the packet given some convoluted conditions (matching window, which should/shouldn't be in use?)
     */
    @Override
    public void handleContainerButtonClick(@NotNull ServerboundContainerButtonClickPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    /**
     * Update the server with an ItemStack in a slot.
     */
    @Override
    public void handleSetCreativeModeSlot(@NotNull ServerboundSetCreativeModeSlotPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleSignUpdate(@NotNull ServerboundSignUpdatePacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    /**
     * Processes a player starting/stopping flying
     */
    @Override
    public void handlePlayerAbilities(@NotNull ServerboundPlayerAbilitiesPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleClientInformation(@NotNull ServerboundClientInformationPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleChangeDifficulty(@NotNull ServerboundChangeDifficultyPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleLockDifficulty(@NotNull ServerboundLockDifficultyPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleChatSessionUpdate(@NotNull ServerboundChatSessionUpdatePacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleConfigurationAcknowledged(@NotNull ServerboundConfigurationAcknowledgedPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    //De lo mas importante
    @Override
    public void handleChunkBatchReceived(@NotNull ServerboundChunkBatchReceivedPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleDebugSampleSubscription(@NotNull ServerboundDebugSampleSubscriptionPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }

    @Override
    public void handleCustomPayload(@NotNull ServerboundCustomPayloadPacket pPacket) {
        WorkerTunnel tunnel = router.route(player.getUUID()); //Select tunnel
        tunnel.send(new WorkerBoundContainerPacket(player.getUUID(),pPacket));
    }
}
