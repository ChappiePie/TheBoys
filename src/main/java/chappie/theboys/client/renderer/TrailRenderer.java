 package chappie.theboys.client.renderer;

 import chappie.modulus.mixin.client.EntityRenderersAccessor;
 import chappie.modulus.util.CommonUtil;
 import chappie.modulus.util.render.IHasContext;
 import chappie.theboys.common.entity.TrailEntity;
 import chappie.theboys.mixin.LivingEntityRendererAccessor;
 import chappie.theboys.util.interfaces.EntitySavingFields;
 import com.mojang.blaze3d.vertex.PoseStack;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.model.EntityModel;
 import net.minecraft.client.model.HumanoidModel;
 import net.minecraft.client.model.PlayerModel;
 import net.minecraft.client.model.geom.ModelLayers;
 import net.minecraft.client.player.AbstractClientPlayer;
 import net.minecraft.client.renderer.MultiBufferSource;
 import net.minecraft.client.renderer.RenderType;
 import net.minecraft.client.renderer.entity.EntityRenderer;
 import net.minecraft.client.renderer.entity.EntityRendererProvider;
 import net.minecraft.client.renderer.entity.LivingEntityRenderer;
 import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
 import net.minecraft.client.renderer.entity.state.PlayerRenderState;
 import net.minecraft.client.renderer.texture.OverlayTexture;
 import net.minecraft.util.ARGB;
 import net.minecraft.world.entity.LivingEntity;
 import org.jetbrains.annotations.NotNull;

 import java.util.Map;

public class TrailRenderer extends EntityRenderer<TrailEntity, TrailRenderState> {

    public TrailRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @NotNull
    @Override
    public TrailRenderState createRenderState() {
        return new TrailRenderState();
    }

    // TODO make own render state
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void extractRenderState(TrailEntity entity, TrailRenderState reusedState, float partialTick) {
        super.extractRenderState(entity, reusedState, partialTick);
        if (entity.attached == null) return;
        reusedState.attached = entity.attached;
        reusedState.distanceToSqr = entity.attached.distanceToSqr(entity);
        LivingEntityRenderState state = null;
        if (EntityRenderersAccessor.providers().get(entity.attached.getType()).create(IHasContext.getContext()) instanceof LivingEntityRenderer renderer) {
            if (renderer.createRenderState(entity.attached, partialTick) instanceof LivingEntityRenderState livingState) {
                state = livingState;
            }
            if (entity.attached instanceof AbstractClientPlayer player) {
                PlayerModel playerModel = new PlayerModel(Minecraft.getInstance().getEntityModels().bakeLayer(
                        CommonUtil.smallArms(player) ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), CommonUtil.smallArms(player));
                playerModel.hat.visible = false;
                playerModel.leftSleeve.visible = false;
                playerModel.rightSleeve.visible = false;
                playerModel.leftPants.visible = false;
                playerModel.rightPants.visible = false;
                playerModel.jacket.visible = false;
                playerModel.setupAnim((PlayerRenderState) state);
                reusedState.model = playerModel;
                reusedState.texture = player.getSkin().texture();
            } else {
                var model1 = (EntityModel<LivingEntityRenderState>) renderer.getModel();
                model1.setupAnim(state);
                if (model1 instanceof HumanoidModel) {
                    ((HumanoidModel<?>) model1).hat.visible = false;
                }
                reusedState.model = model1;
                reusedState.texture = renderer.getTextureLocation(state);
            }
        }

        reusedState.yBodyRot = entity.yBodyRot;
        reusedState.lifeTime = entity.lifeTime;
        reusedState.color = entity.color;
        reusedState.tickCount = entity.tickCount;
        reusedState.partialTick = partialTick;

        reusedState.fieldSavingMap = Map.of("isFallFlying", entity.attached.isFallFlying(),
                "fallFlyingTicks", entity.attached.getFallFlyingTicks(),
                "xRot", entity.attached.getXRot(),
                "yRot", entity.attached.getYRot(),
                "swimAmount", entity.attached.getSwimAmount(1),
                "deltaMovement", entity.attached.getDeltaMovement(),
                "isInWater", entity.attached.isInWater(),
                "isVisuallySwimming", entity.attached.isVisuallySwimming());

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void render(TrailRenderState renderState, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        LivingEntity attached = renderState.attached;
        if (attached == null || renderState.tickCount < 1 || Minecraft.getInstance().options.getCameraType()
                .isFirstPerson() && renderState.distanceToSqr < 10D && renderState.tickCount < 5) return;
        float f = 1F - (renderState.tickCount / (float) renderState.lifeTime);
        float alpha = f / 2.0F;
        f = Math.max(0, 0.5F + f - 0.5F);
        ((EntitySavingFields) attached).theBoys$setup(renderState.fieldSavingMap);
        poseStack.pushPose();
        EntityRenderer<? super LivingEntity, ?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(attached);
        if (renderer instanceof LivingEntityRendererAccessor accessor) {
            LivingEntityRenderState state = (LivingEntityRenderState) renderer.createRenderState(attached, renderState.partialTick);
            accessor.mixin$setupRotations(state, poseStack, renderState.tickCount, renderState.yBodyRot);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            accessor.mixin$scale(renderState, poseStack);
        }
        poseStack.translate(0.0D, -1.501F, 0.0D);
        float red = (renderState.color.getRed() + (int) ((255 - renderState.color.getRed()) * f)) / 255F;
        float green = (renderState.color.getGreen() + (int) ((255 - renderState.color.getGreen()) * f)) / 255F;
        float blue = (renderState.color.getBlue() + (int) ((255 - renderState.color.getBlue()) * f)) / 255F;
        renderState.model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(renderState.texture)), packedLight, OverlayTexture.NO_OVERLAY, ARGB.color((int) (alpha * 255), (int) (red * 255), (int) (green * 255), (int) (blue * 255)));
        poseStack.popPose();
        ((EntitySavingFields) attached).theBoys$reset();
        super.render(renderState, poseStack, bufferSource, packedLight);
    }
}
