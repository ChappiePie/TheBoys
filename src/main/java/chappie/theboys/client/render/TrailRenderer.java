package chappie.theboys.client.render;

import chappie.theboys.common.entities.TrailEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import xyz.heroesunited.heroesunited.util.HUClientUtil;

public class TrailRenderer extends EntityRenderer<TrailEntity> {

    public TrailRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void render(TrailEntity entityIn, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn) {
        if (entityIn.player == null || entityIn.tickCount < 1 || Minecraft.getInstance().options.getCameraType().isFirstPerson() && entityIn.player.distanceToSqr(entityIn) < 3.25D && entityIn.tickCount < 5) return;
        poseStack.pushPose();
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - entityIn.yBodyRot));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0D, -1.501F, 0.0D);
        poseStack.translate((entityIn.getRandom().nextFloat() - 1F) / 80, 0, (entityIn.getRandom().nextFloat() - 1F) / 80);
        entityIn.model.renderToBuffer(poseStack, bufferIn.getBuffer(HUClientUtil.HURenderTypes.LASER), packedLightIn, OverlayTexture.NO_OVERLAY, entityIn.color.getRed() /255F, entityIn.color.getGreen() / 255F, entityIn.color.getBlue() / 255F, (1.0F - (entityIn.tickCount / (float) entityIn.lifeTime)) * 0.5F);
        poseStack.popPose();
        super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(TrailEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
