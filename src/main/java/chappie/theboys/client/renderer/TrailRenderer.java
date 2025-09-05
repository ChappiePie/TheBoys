package chappie.theboys.client.renderer;

import chappie.modulus.util.ClientUtil;
import chappie.theboys.common.entity.TrailEntity;
import chappie.theboys.mixin.LivingEntityRendererAccessor;
import chappie.theboys.util.interfaces.EntitySavingFields;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;

public class TrailRenderer extends EntityRenderer<TrailEntity> {

    public TrailRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void render(TrailEntity entityIn, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn) {
        LivingEntity attached = entityIn.attached;
        if (attached == null || entityIn.tickCount < 1 || Minecraft.getInstance().options.getCameraType()
                .isFirstPerson() && attached.distanceToSqr(entityIn) < 10D && entityIn.tickCount < 5) return;
        float f = 1F - (entityIn.tickCount / (float) entityIn.lifeTime);
        float alpha = f / 2.0F;
        f = Math.max(0, 0.5F + f - 0.5F);
        ((EntitySavingFields) attached).theBoys$setup(entityIn.fieldSavingMap);
        poseStack.pushPose();
        if (Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(attached) instanceof LivingEntityRendererAccessor accessor) {
            accessor.mixin$setupRotations(attached, poseStack, entityIn.tickCount, entityIn.yBodyRot, partialTicks, 1);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            accessor.mixin$scale(attached, poseStack, partialTicks);
        }
        poseStack.translate(0.0D, -1.501F, 0.0D);
        float red = (entityIn.color.getRed() + (int) ((255 - entityIn.color.getRed()) * f)) / 255F;
        float green = (entityIn.color.getGreen() + (int) ((255 - entityIn.color.getGreen()) * f)) / 255F;
        float blue = (entityIn.color.getBlue() + (int) ((255 - entityIn.color.getBlue()) * f)) / 255F;
        entityIn.model.renderToBuffer(poseStack, bufferIn.getBuffer(RenderType.entityTranslucent(entityIn.texture)), packedLightIn, OverlayTexture.NO_OVERLAY, ClientUtil.ARGB.colorFromFloat(alpha, red, green, blue));
        poseStack.popPose();
        ((EntitySavingFields) attached).theBoys$reset();
        super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(TrailEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}