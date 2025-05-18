package com.manwe.dsl.dedicatedServer.worker;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.manwe.dsl.DistributedServerLevels;
import io.netty.util.AttributeKey;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.filters.NetworkFilters;
import net.neoforged.neoforge.network.negotiation.NegotiableNetworkComponent;
import net.neoforged.neoforge.network.negotiation.NegotiationResult;
import net.neoforged.neoforge.network.negotiation.NetworkComponentNegotiator;
import net.neoforged.neoforge.network.payload.*;
import net.neoforged.neoforge.network.registration.NetworkPayloadSetup;
import net.neoforged.neoforge.network.registration.PayloadRegistration;

import java.util.*;

public class ModNetworkRegistry {

    private static final AttributeKey<NetworkPayloadSetup> ATTRIBUTE_PAYLOAD_SETUP = AttributeKey.valueOf("neoforge:payload_setup");
    private static final AttributeKey<Set<ResourceLocation>> ATTRIBUTE_ADHOC_CHANNELS = AttributeKey.valueOf("neoforge:adhoc_channels");
    private static final AttributeKey<ConnectionType> ATTRIBUTE_CONNECTION_TYPE = AttributeKey.valueOf("neoforge:connection_type");
    private static final AttributeKey<PacketFlow> ATTRIBUTE_FLOW = AttributeKey.valueOf("neoforge:flow");

    /**
     * Map of NeoForge payloads that may be sent before channel negotiation.
     * TODO: Separate by protocol + flow.
     */
    private static final Map<ResourceLocation, StreamCodec<FriendlyByteBuf, ? extends CustomPacketPayload>> BUILTIN_PAYLOADS = ImmutableMap.of(
            MinecraftRegisterPayload.ID, MinecraftRegisterPayload.STREAM_CODEC,
            MinecraftUnregisterPayload.ID, MinecraftUnregisterPayload.STREAM_CODEC,
            ModdedNetworkQueryPayload.ID, ModdedNetworkQueryPayload.STREAM_CODEC,
            ModdedNetworkPayload.ID, ModdedNetworkPayload.STREAM_CODEC,
            ModdedNetworkSetupFailedPayload.ID, ModdedNetworkSetupFailedPayload.STREAM_CODEC);

    /**
     * Registry of all custom payload handlers. The initial state of this map should reflect the protocols which support custom payloads.
     * TODO: Change key type to a combination of protocol + flow.
     */
    private static final Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration<?>>> PAYLOAD_REGISTRATIONS = ImmutableMap.of(
            ConnectionProtocol.CONFIGURATION, new HashMap<>(),
            ConnectionProtocol.PLAY, new HashMap<>());

    /**
     * @see net.neoforged.neoforge.network.registration.NetworkRegistry
     */
    public static void initializeNeoForgeConnection(ServerConfigurationPacketListener listener, Map<ConnectionProtocol, Set<ModdedNetworkQueryComponent>> clientChannels) {
        listener.getConnection().channel().attr(ATTRIBUTE_CONNECTION_TYPE).set(listener.getConnectionType());
        listener.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).set(NetworkPayloadSetup.empty());
        listener.getConnection().channel().attr(ATTRIBUTE_FLOW).set(PacketFlow.SERVERBOUND);

        Map<ConnectionProtocol, NegotiationResult> results = new IdentityHashMap<>();

        for (ConnectionProtocol protocol : PAYLOAD_REGISTRATIONS.keySet()) {
            NegotiationResult negotiationResult = NetworkComponentNegotiator.negotiate(
                    PAYLOAD_REGISTRATIONS.get(protocol).values().stream().map(NegotiableNetworkComponent::new).toList(),
                    clientChannels.getOrDefault(protocol, Collections.emptySet()).stream().map(NegotiableNetworkComponent::new).toList());

            // Negotiation failed. Disconnect the client.
            if (!negotiationResult.success()) {
                if (!negotiationResult.failureReasons().isEmpty()) {
                    //TODO gestionar respuestas
                    DistributedServerLevels.LOGGER.error("Negotiation failed");
                    //listener.send(new ModdedNetworkSetupFailedPayload(negotiationResult.failureReasons()));
                }

                listener.disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s".formatted(NeoForgeVersion.getVersion())));
                return;
            }
            results.put(protocol, negotiationResult);
        }

        NetworkPayloadSetup setup = NetworkPayloadSetup.from(results);

        listener.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).set(setup);

        NetworkFilters.injectIfNecessary(listener.getConnection());

        //listener.send(new ModdedNetworkPayload(setup));
        ImmutableSet.Builder<ResourceLocation> nowListeningOn = ImmutableSet.builder();
        nowListeningOn.addAll(getInitialListeningChannels(listener.flow()));
        nowListeningOn.addAll(setup.getChannels(ConnectionProtocol.CONFIGURATION).keySet());
        //listener.send(new MinecraftRegisterPayload(nowListeningOn.build()));
    }

    /**
     * {@return the initial channels for the configuration phase.}
     */
    public static Set<ResourceLocation> getInitialListeningChannels(PacketFlow flow) {
        return BUILTIN_PAYLOADS.keySet();
    }
}
