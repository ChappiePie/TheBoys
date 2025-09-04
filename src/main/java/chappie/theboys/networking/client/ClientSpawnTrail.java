package chappie.theboys.networking.client;

import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.render.IHasContext;
import chappie.theboys.TheBoys;
import chappie.theboys.common.entity.TrailEntity;
import chappie.theboys.mixin.client.EntityRenderersAccessor;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.awt.*;
import java.util.Map;

import static net.minecraft.client.renderer.entity.LivingEntityRenderer.isEntityUpsideDown;

public class ClientSpawnTrail implements CustomPacketPayload {

    public static final ResourceLocation PACKET_ID = TheBoys.id("spawn_trail");
    public static final Type<ClientSpawnTrail> PACKET = new Type<>(PACKET_ID);
    public static StreamCodec<FriendlyByteBuf, ClientSpawnTrail> CODEC = CustomPacketPayload.codec(ClientSpawnTrail::write, ClientSpawnTrail::new);
    public final TrailEntity entity;
    public final int entityId;
    public final int lifeTime, ownerId;
    public final Color color;

    public ClientSpawnTrail(TrailEntity e) {
        this.entity = e;
        this.entityId = e.getId();
        this.lifeTime = e.lifeTime;
        if (e.attached != null) {
            this.ownerId = e.attached.getId();
        } else {
            this.ownerId = -1;
        }
        this.color = e.color;
    }

    public ClientSpawnTrail(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    private ClientSpawnTrail(int entityId, int lifeTime, int ownerId, int red, int green, int blue) {
        this.entity = null;
        this.entityId = entityId;
        this.lifeTime = lifeTime;
        this.ownerId = ownerId;
        this.color = new Color(red, green, blue);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.lifeTime);
        buf.writeInt(this.ownerId);

        buf.writeInt(this.color.getRed());
        buf.writeInt(this.color.getGreen());
        buf.writeInt(this.color.getBlue());
    }

    @SuppressWarnings("unchecked")
    public void handle(LocalPlayer localPlayer, PacketSender packetSender) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Entity entity = mc.level.getEntity(this.entityId);
            if (entity instanceof TrailEntity e) {
                e.lifeTime = this.lifeTime;
                e.attached = (LivingEntity) entity.getCommandSenderWorld().getEntity(this.ownerId);
                e.color = this.color;
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
    }
}