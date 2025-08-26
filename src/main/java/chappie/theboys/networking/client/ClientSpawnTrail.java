package chappie.theboys.networking.client;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.render.IHasContext;
import chappie.theboys.TheBoys;
import chappie.theboys.client.renderer.TrailRenderState;
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
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.awt.*;
import java.util.Map;

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
        this.ownerId = e.attached.getId();
        this.color = e.color;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET;
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

                e.yBodyRot = e.attached.yBodyRot;

                e.fieldSavingMap = Map.of("isFallFlying", e.attached.isFallFlying(),
                        "fallFlyingTicks", e.attached.getFallFlyingTicks(),
                        "xRot", e.attached.getXRot(),
                        "yRot", e.attached.getYRot(),
                        "swimAmount", e.attached.getSwimAmount(1),
                        "deltaMovement", e.attached.getDeltaMovement(),
                        "isInWater", e.attached.isInWater(),
                        "isVisuallySwimming", e.attached.isVisuallySwimming());


                LivingEntityRenderState state = null;
                var r = EntityRenderersAccessor.providers().get(e.attached.getType());
                if (r == null && e.attached instanceof AbstractClientPlayer player) {
                    r = EntityRenderersAccessor.playerProviders().get(player.getSkin().model());
                }
                if (r.create(IHasContext.getContext()) instanceof LivingEntityRenderer renderer) {
                    if (renderer.createRenderState(e.attached, ClientUtil.getPartialTick()) instanceof LivingEntityRenderState livingState) {
                        state = livingState;
                    }
                    if (e.attached instanceof AbstractClientPlayer player) {
                        PlayerModel playerModel = new PlayerModel(Minecraft.getInstance().getEntityModels().bakeLayer(
                                CommonUtil.smallArms(player) ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), CommonUtil.smallArms(player));
                        playerModel.hat.visible = false;
                        playerModel.leftSleeve.visible = false;
                        playerModel.rightSleeve.visible = false;
                        playerModel.leftPants.visible = false;
                        playerModel.rightPants.visible = false;
                        playerModel.jacket.visible = false;
                        playerModel.setupAnim((PlayerRenderState) state);
                        e.trail = new TrailRenderState.TrailResources(playerModel, player.getSkin().texture());
                    } else {
                        var model1 = (EntityModel<LivingEntityRenderState>) renderer.getModel();
                        model1.setupAnim(state);
                        if (model1 instanceof HumanoidModel) {
                            ((HumanoidModel<?>) model1).hat.visible = false;
                        }
                        e.trail = new TrailRenderState.TrailResources(model1, renderer.getTextureLocation(state));
                    }
                }
            }
        }
    }
}