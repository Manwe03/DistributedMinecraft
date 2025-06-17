package com.manwe.dsl.dedicatedServer.worker;

import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.dedicatedServer.proxy.back.packets.ProxyBoundContainerPacket;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.*;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class FakePlayerConnection extends Connection {
    private final UUID playerId;
    private final RegistryAccess workerRegistryAccess;
    private static final Set<PacketType<?>> ALLOWED = Set.of(
        GamePacketTypes.CLIENTBOUND_BUNDLE,
        GamePacketTypes.CLIENTBOUND_BUNDLE_DELIMITER,
        GamePacketTypes.CLIENTBOUND_ADD_ENTITY,
        GamePacketTypes.CLIENTBOUND_ADD_EXPERIENCE_ORB,
        GamePacketTypes.CLIENTBOUND_ANIMATE,
        //GamePacketTypes.CLIENTBOUND_AWARD_STATS,
        GamePacketTypes.CLIENTBOUND_BLOCK_CHANGED_ACK,
        GamePacketTypes.CLIENTBOUND_BLOCK_DESTRUCTION,
        GamePacketTypes.CLIENTBOUND_BLOCK_ENTITY_DATA,
        GamePacketTypes.CLIENTBOUND_BLOCK_EVENT,
        GamePacketTypes.CLIENTBOUND_BLOCK_UPDATE,
        //GamePacketTypes.CLIENTBOUND_BOSS_EVENT,
        //GamePacketTypes.CLIENTBOUND_CHANGE_DIFFICULTY,
        GamePacketTypes.CLIENTBOUND_CHUNK_BATCH_FINISHED,
        GamePacketTypes.CLIENTBOUND_CHUNK_BATCH_START,
        GamePacketTypes.CLIENTBOUND_CHUNKS_BIOMES, //??
        //GamePacketTypes.CLIENTBOUND_CLEAR_TITLES, //??
        //GamePacketTypes.CLIENTBOUND_COMMAND_SUGGESTIONS,
        //GamePacketTypes.CLIENTBOUND_COMMANDS,
        GamePacketTypes.CLIENTBOUND_CONTAINER_CLOSE,
        GamePacketTypes.CLIENTBOUND_CONTAINER_SET_CONTENT,
        GamePacketTypes.CLIENTBOUND_CONTAINER_SET_DATA,
        GamePacketTypes.CLIENTBOUND_CONTAINER_SET_SLOT,
        GamePacketTypes.CLIENTBOUND_COOLDOWN,
        //GamePacketTypes.CLIENTBOUND_CUSTOM_CHAT_COMPLETIONS,
        GamePacketTypes.CLIENTBOUND_DAMAGE_EVENT, //TODO ver si damage event incluye al jugador
        //GamePacketTypes.CLIENTBOUND_DEBUG_SAMPLE,
        //GamePacketTypes.CLIENTBOUND_DELETE_CHAT,
        //GamePacketTypes.CLIENTBOUND_DISGUISED_CHAT,
        GamePacketTypes.CLIENTBOUND_ENTITY_EVENT,
        GamePacketTypes.CLIENTBOUND_EXPLODE,
        GamePacketTypes.CLIENTBOUND_FORGET_LEVEL_CHUNK,
        //GamePacketTypes.CLIENTBOUND_GAME_EVENT,
        GamePacketTypes.CLIENTBOUND_HORSE_SCREEN_OPEN, //??
        GamePacketTypes.CLIENTBOUND_HURT_ANIMATION,
        //GamePacketTypes.CLIENTBOUND_INITIALIZE_BORDER,
        GamePacketTypes.CLIENTBOUND_LEVEL_CHUNK_WITH_LIGHT,
        //GamePacketTypes.CLIENTBOUND_LEVEL_EVENT,
        GamePacketTypes.CLIENTBOUND_LEVEL_PARTICLES,
        GamePacketTypes.CLIENTBOUND_LIGHT_UPDATE,
        //GamePacketTypes.CLIENTBOUND_LOGIN,
        GamePacketTypes.CLIENTBOUND_MAP_ITEM_DATA, //??
        GamePacketTypes.CLIENTBOUND_MERCHANT_OFFERS, //??
        GamePacketTypes.CLIENTBOUND_MOVE_ENTITY_POS,
        GamePacketTypes.CLIENTBOUND_MOVE_ENTITY_POS_ROT,
        GamePacketTypes.CLIENTBOUND_MOVE_ENTITY_ROT,
        GamePacketTypes.CLIENTBOUND_MOVE_VEHICLE,
        //GamePacketTypes.CLIENTBOUND_OPEN_BOOK,
        GamePacketTypes.CLIENTBOUND_OPEN_SCREEN,
        GamePacketTypes.CLIENTBOUND_OPEN_SIGN_EDITOR,
        GamePacketTypes.CLIENTBOUND_PLACE_GHOST_RECIPE,
        //GamePacketTypes.CLIENTBOUND_PLAYER_ABILITIES,
        //GamePacketTypes.CLIENTBOUND_PLAYER_CHAT, TODO rehacer el chat
        //GamePacketTypes.CLIENTBOUND_PLAYER_COMBAT_END,
        //GamePacketTypes.CLIENTBOUND_PLAYER_COMBAT_ENTER,
        //GamePacketTypes.CLIENTBOUND_PLAYER_COMBAT_KILL,
        //GamePacketTypes.CLIENTBOUND_PLAYER_INFO_REMOVE,
        //GamePacketTypes.CLIENTBOUND_PLAYER_INFO_UPDATE,
        //GamePacketTypes.CLIENTBOUND_PLAYER_LOOK_AT,
        //GamePacketTypes.CLIENTBOUND_PLAYER_POSITION,
        //GamePacketTypes.CLIENTBOUND_RECIPE,
        GamePacketTypes.CLIENTBOUND_REMOVE_ENTITIES, //TODO esto hay que verlo
        GamePacketTypes.CLIENTBOUND_REMOVE_MOB_EFFECT,
        //GamePacketTypes.CLIENTBOUND_RESPAWN,
        GamePacketTypes.CLIENTBOUND_ROTATE_HEAD, //TODO ver si incluye al player
        GamePacketTypes.CLIENTBOUND_SECTION_BLOCKS_UPDATE,
        //GamePacketTypes.CLIENTBOUND_SELECT_ADVANCEMENTS_TAB,
        //GamePacketTypes.CLIENTBOUND_SERVER_DATA, //??
        //GamePacketTypes.CLIENTBOUND_SET_ACTION_BAR_TEXT,
        //GamePacketTypes.CLIENTBOUND_SET_BORDER_CENTER,
        //GamePacketTypes.CLIENTBOUND_SET_BORDER_LERP_SIZE,
        //GamePacketTypes.CLIENTBOUND_SET_BORDER_SIZE,
        //GamePacketTypes.CLIENTBOUND_SET_BORDER_WARNING_DELAY,
        //GamePacketTypes.CLIENTBOUND_SET_BORDER_WARNING_DISTANCE,
        //GamePacketTypes.CLIENTBOUND_SET_CAMERA,
        //GamePacketTypes.CLIENTBOUND_SET_CARRIED_ITEM,
        //GamePacketTypes.CLIENTBOUND_SET_CHUNK_CACHE_CENTER,
        //GamePacketTypes.CLIENTBOUND_SET_CHUNK_CACHE_RADIUS,
        //GamePacketTypes.CLIENTBOUND_SET_DEFAULT_SPAWN_POSITION,
        //GamePacketTypes.CLIENTBOUND_SET_DISPLAY_OBJECTIVE,
        GamePacketTypes.CLIENTBOUND_SET_ENTITY_DATA,
        GamePacketTypes.CLIENTBOUND_SET_ENTITY_LINK,
        GamePacketTypes.CLIENTBOUND_SET_ENTITY_MOTION,
        GamePacketTypes.CLIENTBOUND_SET_EQUIPMENT, //TODO ver si incluye al player
        //GamePacketTypes.CLIENTBOUND_SET_EXPERIENCE,
        //GamePacketTypes.CLIENTBOUND_SET_HEALTH,
        //GamePacketTypes.CLIENTBOUND_SET_OBJECTIVE,
        //GamePacketTypes.CLIENTBOUND_SET_PASSENGERS,
        //GamePacketTypes.CLIENTBOUND_SET_PLAYER_TEAM,
        //GamePacketTypes.CLIENTBOUND_SET_SCORE ,
        //GamePacketTypes.CLIENTBOUND_SET_SIMULATION_DISTANCE ,
        //GamePacketTypes.CLIENTBOUND_SET_SUBTITLE_TEXT ,
        //GamePacketTypes.CLIENTBOUND_SET_TIME ,
        //GamePacketTypes.CLIENTBOUND_SET_TITLE_TEXT ,
        //GamePacketTypes.CLIENTBOUND_SET_TITLES_ANIMATION ,
        GamePacketTypes.CLIENTBOUND_SOUND_ENTITY ,
        //GamePacketTypes.CLIENTBOUND_SOUND, //¿Qué diferencia hay?
        //GamePacketTypes.CLIENTBOUND_START_CONFIGURATION,
        GamePacketTypes.CLIENTBOUND_STOP_SOUND,
        //GamePacketTypes.CLIENTBOUND_SYSTEM_CHAT,
        //GamePacketTypes.CLIENTBOUND_TAB_LIST,
        //GamePacketTypes.CLIENTBOUND_TAG_QUERY,
        //GamePacketTypes.CLIENTBOUND_TAKE_ITEM_ENTITY,
        //GamePacketTypes.CLIENTBOUND_TELEPORT_ENTITY,
        //GamePacketTypes.CLIENTBOUND_UPDATE_ADVANCEMENTS,
        //GamePacketTypes.CLIENTBOUND_UPDATE_ATTRIBUTES,
        GamePacketTypes.CLIENTBOUND_UPDATE_MOB_EFFECT //TODO ver si incluye al player
        //GamePacketTypes.CLIENTBOUND_UPDATE_RECIPES,
        //GamePacketTypes.CLIENTBOUND_PROJECTILE_POWER
    );
    
    public FakePlayerConnection(PacketFlow pReceiving, UUID playerId, RegistryAccess workerRegistryAccess) {
        super(pReceiving);
        this.playerId = playerId;
        this.workerRegistryAccess = workerRegistryAccess;
    }

    /**
     * <img src="https://i.imgur.com/zXZslJX.jpeg">
     * <P>Wrap all messages in the ProxyBoundContainerPacket to be sent through the same ChannelPipeline</P>
     * <P>All packets should be of type Packet<ClientGamePacketListener></P>
     */
    @Override
    public void send(Packet<?> pPacket, @Nullable PacketSendListener pListener, boolean pFlush) {
        try {
            if(ALLOWED.contains(pPacket.type())){
                //System.out.println("Allowed: "+pPacket.type());
                ProxyBoundContainerPacket newPacket = new ProxyBoundContainerPacket(playerId, (Packet<ClientGamePacketListener>) pPacket, this.workerRegistryAccess);
                super.send(newPacket, pListener, pFlush);
            }else {
                //System.out.println("Fake player tried to send but blocked: "+pPacket.type());
            }
        }catch (Exception e){
            DistributedServerLevels.LOGGER.warn("Worker tried to send an unknown packet", e);
        }
    }

    @Override
    public void handleDisconnection() {
        super.handleDisconnection();
    }
}
