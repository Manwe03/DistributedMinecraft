package com.manwe.dsl.dedicatedServer.worker.listeners;

import com.google.common.primitives.Floats;
import com.manwe.dsl.DistributedServerLevels;
import com.manwe.dsl.mixin.accessors.ServerGamePacketListenerImplAccessor;
import com.manwe.dsl.mixin.invokers.ServerGamePacketListenerImplInvoker;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.Set;

public class WorkerGamePacketListenerImpl extends ServerGamePacketListenerImpl {

    private boolean feedbackSent = false;

    public WorkerGamePacketListenerImpl(MinecraftServer pServer, Connection pConnection, ServerPlayer pPlayer, CommonListenerCookie pCookie) {
        super(pServer, pConnection, pPlayer, pCookie);
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

        if(!feedbackSent) {
            feedbackSent = true;
            this.teleport(player.getX(),player.getY(),player.getZ(),player.getXRot(),player.getYRot());
            this.teleport(player.getX(),player.getY(),player.getZ(),player.getXRot(),player.getYRot());
        }
    }

    /**
     * <h1>LA SOLUCIÓN NUCLEAR</h1>
     */
    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket pPacket) {

        player.tick();

        System.out.println("handleMovePlayer: "+pPacket.getX(0)+":"+pPacket.getY(0)+":"+pPacket.getZ(0));
        //super.handleMovePlayer(pPacket);

        System.out.println("Antes de mover: " + player.getX() + ":" + player.getY() + ":" + player.getZ() + " tickCount=" + player.tickCount + " isAlive=" + player.isAlive() + " isSleeping=" + player.isSleeping() + " noPhysics=" + player.noPhysics);

        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        if (containsInvalidValues(pPacket.getX(0.0), pPacket.getY(0.0), pPacket.getZ(0.0), pPacket.getYRot(0.0F), pPacket.getXRot(0.0F))) {
            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
        } else {
            ServerLevel serverlevel = this.player.serverLevel();
            if (!this.player.wonGame) {
                if (((ServerGamePacketListenerImplAccessor)this).getTickCount() == 0) {
                    this.resetPosition();
                }

                if (!localInvokeUpdateAwaitingTeleport()) {
                    double d0 = clampHorizontal(pPacket.getX(this.player.getX()));
                    double d1 = clampVertical(pPacket.getY(this.player.getY()));
                    double d2 = clampHorizontal(pPacket.getZ(this.player.getZ()));
                    float f = Mth.wrapDegrees(pPacket.getYRot(this.player.getYRot()));
                    float f1 = Mth.wrapDegrees(pPacket.getXRot(this.player.getXRot()));
                    if (this.player.isPassenger()) {
                        this.player.absMoveTo(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                        System.out.println("absMoveTo isPassenger");
                        this.player.serverLevel().getChunkSource().move(this.player);
                    } else {
                        double d3 = this.player.getX();
                        double d4 = this.player.getY();
                        double d5 = this.player.getZ();
                        double d6 = d0 - ((ServerGamePacketListenerImplAccessor)this).getFirstGoodX();
                        double d7 = d1 - ((ServerGamePacketListenerImplAccessor)this).getFirstGoodY();
                        double d8 = d2 - ((ServerGamePacketListenerImplAccessor)this).getFirstGoodZ();
                        double d9 = this.player.getDeltaMovement().lengthSqr();
                        double d10 = d6 * d6 + d7 * d7 + d8 * d8;
                        if (this.player.isSleeping()) {
                            if (d10 > 1.0) {
                                System.out.println("Sleeping TELEPORT");
                                this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                            }
                        } else {
                            boolean flag = this.player.isFallFlying();
                            if (serverlevel.tickRateManager().runsNormally()) {
                                ((ServerGamePacketListenerImplAccessor)this).setReceivedMovePacketCount(((ServerGamePacketListenerImplAccessor)this).getReceivedMovePacketCount()+1);
                                int i = ((ServerGamePacketListenerImplAccessor)this).getReceivedMovePacketCount() - ((ServerGamePacketListenerImplAccessor)this).getKnownMovePacketCount();
                                if (i > 5) {
                                    DistributedServerLevels.LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", this.player.getName().getString(), i);
                                    i = 1;
                                }

                                if (!this.player.isChangingDimension()
                                        && (!this.player.level().getGameRules().getBoolean(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK) || !flag)) {
                                    float f2 = flag ? 300.0F : 100.0F;
                                    if (d10 - d9 > (double)(f2 * (float)i) && !this.isSingleplayerOwner()) {
                                        DistributedServerLevels.LOGGER.warn("{} moved too quickly! {},{},{}", this.player.getName().getString(), d6, d7, d8);
                                        System.out.println("quickly TELEPORT");
                                        this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYRot(), this.player.getXRot());
                                        return;
                                    }
                                }
                            }

                            AABB aabb = this.player.getBoundingBox();
                            d6 = d0 - ((ServerGamePacketListenerImplAccessor)this).getLastGoodX();
                            d7 = d1 - ((ServerGamePacketListenerImplAccessor)this).getLastGoodY();
                            d8 = d2 - ((ServerGamePacketListenerImplAccessor)this).getLastGoodZ();
                            boolean flag4 = d7 > 0.0;
                            if (this.player.onGround() && !pPacket.isOnGround() && flag4) {
                                this.player.jumpFromGround();
                            }

                            boolean flag1 = this.player.verticalCollisionBelow;
                            this.player.move(MoverType.PLAYER, new Vec3(d6, d7, d8));
                            d6 = d0 - this.player.getX();
                            d7 = d1 - this.player.getY();
                            if (d7 > -0.5 || d7 < 0.5) {
                                d7 = 0.0;
                            }

                            d8 = d2 - this.player.getZ();
                            d10 = d6 * d6 + d7 * d7 + d8 * d8;
                            boolean flag2 = false;
                            if (!this.player.isChangingDimension()
                                    && d10 > 0.0625
                                    && !this.player.isSleeping()
                                    && !this.player.gameMode.isCreative()
                                    && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
                                flag2 = true;
                                DistributedServerLevels.LOGGER.warn("{} moved wrongly!", this.player.getName().getString());
                            }

                            System.out.println("this.player.noPhysics "+ this.player.noPhysics+" o");
                            System.out.println("this.player.isSleeping() "+ this.player.isSleeping()+" o");
                            System.out.println("(!flag2 || !serverlevel.noCollision(this.player, aabb))"+(!flag2 || !serverlevel.noCollision(this.player, aabb))+" y");
                            System.out.println("! localInvokeIsPlayerCollidingWithAnythingNew(serverlevel, aabb, d0, d1, d2)"+ !localInvokeIsPlayerCollidingWithAnythingNew(serverlevel, aabb, d0, d1, d2));

                            if (this.player.noPhysics || this.player.isSleeping() || (!flag2 || !serverlevel.noCollision(this.player, aabb)) && !localInvokeIsPlayerCollidingWithAnythingNew(serverlevel, aabb, d0, d1, d2)) {
                                this.player.absMoveTo(d0, d1, d2, f, f1);
                                boolean flag3 = this.player.isAutoSpinAttack();
                                ((ServerGamePacketListenerImplAccessor)this).setClientIsFloating(d7 >= -0.03125
                                        && !flag1
                                        && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR
                                        && !this.server.isFlightAllowed()
                                        && !this.player.mayFly()
                                        && !this.player.hasEffect(MobEffects.LEVITATION)
                                        && !flag
                                        && !flag3
                                        && localInvoekNoBlocksAround(this.player));
                                this.player.serverLevel().getChunkSource().move(this.player);
                                Vec3 vec3 = new Vec3(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5);
                                this.player.setOnGroundWithMovement(pPacket.isOnGround(), vec3);
                                this.player.doCheckFallDamage(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5, pPacket.isOnGround());
                                this.player.setKnownMovement(vec3);
                                if (flag4) {
                                    this.player.resetFallDistance();
                                }

                                if (pPacket.isOnGround()
                                        || this.player.hasLandedInLiquid()
                                        || this.player.onClimbable()
                                        || this.player.isSpectator()
                                        || flag
                                        || flag3) {
                                    this.player.tryResetCurrentImpulseContext();
                                }

                                this.player.checkMovementStatistics(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5);
                                ((ServerGamePacketListenerImplAccessor) this).setLastGoodX(this.player.getX());
                                ((ServerGamePacketListenerImplAccessor) this).setLastGoodY(this.player.getY());
                                ((ServerGamePacketListenerImplAccessor) this).setLastGoodZ(this.player.getZ());
                                System.out.println("NO EXTRA");
                            } else {
                                System.out.println("TELEPORT EXTRA");
                                this.teleport(d3, d4, d5, f, f1);
                                this.player.doCheckFallDamage(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5, pPacket.isOnGround());
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Tras de mover: " + player.getX() + ":" + player.getY() + ":" + player.getZ() + " tickCount=" + player.tickCount + " isAlive=" + player.isAlive() + " isSleeping=" + player.isSleeping() + " noPhysics=" + player.noPhysics);
        System.out.println("AwaitingTeleport: " + ((ServerGamePacketListenerImplAccessor)this).getAwaitingTeleport() + " AwaitingPos: " + ((ServerGamePacketListenerImplAccessor)this).getAwaitingPositionFromClient());

    }

    private static boolean containsInvalidValues(double pX, double pY, double pZ, float pYRot, float pXRot) {
        return Double.isNaN(pX) || Double.isNaN(pY) || Double.isNaN(pZ) || !Floats.isFinite(pXRot) || !Floats.isFinite(pYRot);
    }
    private static double clampHorizontal(double pValue) {
        return Mth.clamp(pValue, -3.0E7, 3.0E7);
    }
    private static double clampVertical(double pValue) {
        return Mth.clamp(pValue, -2.0E7, 2.0E7);
    }

    private boolean localInvokeUpdateAwaitingTeleport(){
        return ((ServerGamePacketListenerImplInvoker)this).invokeUpdateAwaitingTeleport();
    }

    private boolean localInvokeIsPlayerCollidingWithAnythingNew(LevelReader pLevel, AABB pBox, double pX, double pY, double pZ){
        return ((ServerGamePacketListenerImplInvoker)this).invokeIsPlayerCollidingWithAnythingNew(pLevel, pBox, pX, pY, pZ);
    }
    private boolean localInvoekNoBlocksAround(Entity pEntity){
        return ((ServerGamePacketListenerImplInvoker)this).invokeNoBlocksAround(pEntity);
    }

    @Override
    public void teleport(double pX, double pY, double pZ, float pYaw, float pPitch, Set<RelativeMovement> pRelativeSet) {
        super.teleport( pX,  pY,  pZ,  pYaw,  pPitch,  pRelativeSet);
        System.out.println("teleport -> AwaitingTeleport: " + ((ServerGamePacketListenerImplAccessor)this).getAwaitingTeleport() + " AwaitingPos: " + ((ServerGamePacketListenerImplAccessor)this).getAwaitingPositionFromClient().toString());
    }

    @Override
    public void handleKeepAlive(ServerboundKeepAlivePacket pPacket) {
        super.handleKeepAlive(pPacket);
        System.out.println("(NO debería) keep alive recibido en el worker");
    }

    @Override
    protected void keepConnectionAlive() {
        //Desactiva el keep alive del worker, esto lo maneja solo el proxy
        //super.keepConnectionAlive();
    }
}
