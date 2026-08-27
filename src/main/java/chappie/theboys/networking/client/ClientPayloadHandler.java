package chappie.theboys.networking.client;

import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.render.IHasContext;
import chappie.theboys.common.entity.TrailEntity;
import chappie.theboys.mixin.client.EntityRenderersAccessor;
import chappie.theboys.networking.packet.SyncTheBoysCapPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Map;

import static net.minecraft.client.renderer.entity.LivingEntityRenderer.isEntityUpsideDown;

/**
 * Handles client-bound packets. This class is only ever loaded on the CLIENT dist.
 */
public class ClientPayloadHandler {

    public static void registerAll(PayloadRegistrar registrar) {
        registrar.playToClient(SyncTheBoysCapPacket.PACKET, SyncTheBoysCapPacket.CODEC, ClientPayloadHandler::handleSyncTheBoysCapPacket);
        registrar.playToClient(ClientSpawnTrail.PACKET, ClientSpawnTrail.CODEC, ClientPayloadHandler::handleClientSpawnTrail);
    }

    public static void handleSyncTheBoysCapPacket(SyncTheBoysCapPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof LocalPlayer localPlayer) {
                packet.handle(localPlayer);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static void handleClientSpawnTrail(ClientSpawnTrail packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(packet.entityId);
                if (entity instanceof TrailEntity e) {
                    e.lifeTime = packet.lifeTime;
                    e.attached = (LivingEntity) entity.getCommandSenderWorld().getEntity(packet.ownerId);
                    e.color = packet.color;
                    if (e.attached == null) return;

                    if (e.attached instanceof AbstractClientPlayer player) {
                        PlayerModel<LivingEntity> model = new PlayerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(
                                CommonUtil.smallArms(player) ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), CommonUtil.smallArms(player));
                        model.hat.visible = false;
                        model.leftSleeve.visible = false;
                        model.rightSleeve.visible = false;
                        model.leftPants.visible = false;
                        model.rightPants.visible = false;
                        model.jacket.visible = false;
                        e.model = model;
                        e.texture = player.getSkin().texture();
                    } else {
                        if (EntityRenderersAccessor.providers().get(e.attached.getType()).create(IHasContext.getContext()) instanceof LivingEntityRenderer renderer) {
                            e.model = (EntityModel<LivingEntity>) renderer.getModel();
                            e.texture = renderer.getTextureLocation(e.attached);
                        }
                    }

                    e.fieldSavingMap = Map.of("isFallFlying", e.attached.isFallFlying(),
                            "fallFlyingTicks", e.attached.getFallFlyingTicks(),
                            "xRot", e.attached.getXRot(),
                            "yRot", e.attached.getYRot(),
                            "swimAmount", e.attached.getSwimAmount(1),
                            "deltaMovement", e.attached.getDeltaMovement(),
                            "isInWater", e.attached.isInWater(),
                            "isVisuallySwimming", e.attached.isVisuallySwimming());

                    {
                        e.yBodyRot = e.attached.yBodyRot;
                        e.model.attackTime = e.attached.getAttackAnim(0);
                        e.model.riding = e.attached.isPassenger();
                        e.model.young = e.attached.isBaby();
                        float f = Mth.rotLerp(0, e.attached.yBodyRotO, e.attached.yBodyRot);
                        float f1 = Mth.rotLerp(0, e.attached.yHeadRotO, e.attached.yHeadRot);
                        float f2 = f1 - f;
                        if (e.attached.isPassenger() && e.attached.getVehicle() instanceof LivingEntity livingentity) {
                            f = Mth.rotLerp(0, livingentity.yBodyRotO, livingentity.yBodyRot);
                            f2 = f1 - f;
                            float f3 = Mth.wrapDegrees(f2);
                            if (f3 < -85.0F) {
                                f3 = -85.0F;
                            }

                            if (f3 >= 85.0F) {
                                f3 = 85.0F;
                            }

                            f = f1 - f3;
                            if (f3 * f3 > 2500.0F) {
                                f += f3 * 0.2F;
                            }

                            f2 = f1 - f;
                        }

                        float f6 = Mth.lerp(0, e.attached.xRotO, e.attached.getXRot());
                        if (isEntityUpsideDown(e.attached)) {
                            f6 *= -1.0F;
                            f2 *= -1.0F;
                        }

                        float f8 = 0.0F;
                        float f5 = 0.0F;
                        if (!e.attached.isPassenger() && e.attached.isAlive()) {
                            f8 = e.attached.walkAnimation.speed(1);
                            f5 = e.attached.walkAnimation.position(1);
                            if (e.attached.isBaby()) {
                                f5 *= 3.0F;
                            }

                            if (f8 > 1.0F) {
                                f8 = 1.0F;
                            }
                        }

                        if (e.model instanceof HumanoidModel<?> model) {
                            model.hat.visible = false;
                            model.crouching = e.attached.isCrouching();
                        }
                        e.model.prepareMobModel(e.attached, f5, f8, 0);
                        e.model.setupAnim(e.attached, f5, f8, e.attached.tickCount, f2, f6);
                    }
                }
            }
        });
    }
}
