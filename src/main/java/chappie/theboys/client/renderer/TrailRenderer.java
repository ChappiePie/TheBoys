 package chappie.theboys.client.renderer;

 import chappie.theboys.common.entity.TrailEntity;
 import chappie.theboys.mixin.LivingEntityRendererAccessor;
 import chappie.theboys.util.TBClientUtil;
 import chappie.theboys.util.interfaces.EntitySavingFields;
 import com.mojang.blaze3d.vertex.PoseStack;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.MultiBufferSource;
 import net.minecraft.client.renderer.entity.EntityRenderer;
 import net.minecraft.client.renderer.entity.EntityRendererProvider;
 import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
 import net.minecraft.client.renderer.texture.OverlayTexture;
 import net.minecraft.util.ARGB;
 import net.minecraft.world.entity.LivingEntity;
 import org.jetbrains.annotations.NotNull;

 public class TrailRenderer extends EntityRenderer<TrailEntity, TrailRenderState> {

    public TrailRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @NotNull
    @Override
    public TrailRenderState createRenderState() {
        return new TrailRenderState();
    }

    @Override
    public void extractRenderState(TrailEntity entity, TrailRenderState reusedState, float partialTick) {
        super.extractRenderState(entity, reusedState, partialTick);
        if (entity.attached == null) return;
        reusedState.attached = entity.attached;
        reusedState.distanceToSqr = entity.attached.distanceToSqr(entity);
        reusedState.yBodyRot = entity.yBodyRot;
        reusedState.lifeTime = entity.lifeTime;
        reusedState.color = entity.color;
        reusedState.tickCount = entity.tickCount;
        reusedState.partialTick = partialTick;
        reusedState.fieldSavingMap = entity.fieldSavingMap;

        reusedState.trail = entity.trail;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void render(TrailRenderState renderState, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        LivingEntity attached = renderState.attached;
        if (attached == null || renderState.trail == null || renderState.tickCount < 1 || Minecraft.getInstance().options.getCameraType()
                .isFirstPerson() && renderState.distanceToSqr < 10D && renderState.tickCount < 5) return;
        float f = 1F - (renderState.tickCount / (float) renderState.lifeTime);
        float alpha = f / 2.0F;
        f = Math.max(0, 0.5F + f - 0.5F);
        ((EntitySavingFields) attached).theBoys$setup(renderState.fieldSavingMap);
        poseStack.pushPose();
        EntityRenderer<? super LivingEntity, ?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(attached);
        if (renderer instanceof LivingEntityRendererAccessor accessor) {
            LivingEntityRenderState state = (LivingEntityRenderState) renderer.createRenderState(attached, renderState.partialTick);
            accessor.mixin$setupRotations(state, poseStack, renderState.yBodyRot, renderState.scale);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            accessor.mixin$scale(state, poseStack);
        }
        poseStack.translate(0.0D, -1.501F, 0.0D);
        float red = (renderState.color.getRed() + (int) ((255 - renderState.color.getRed()) * f)) / 255F;
        float green = (renderState.color.getGreen() + (int) ((255 - renderState.color.getGreen()) * f)) / 255F;
        float blue = (renderState.color.getBlue() + (int) ((255 - renderState.color.getBlue()) * f)) / 255F;
        renderState.trail.model().renderToBuffer(poseStack, bufferSource.getBuffer(TBClientUtil.RenderTypes.entityTranslucent(renderState.trail.texture())), packedLight, OverlayTexture.NO_OVERLAY, ARGB.color((int) (alpha * 255), (int) (red * 255), (int) (green * 255), (int) (blue * 255)));
        poseStack.popPose();
        ((EntitySavingFields) attached).theBoys$reset();
        super.render(renderState, poseStack, bufferSource, packedLight);
    }
}
