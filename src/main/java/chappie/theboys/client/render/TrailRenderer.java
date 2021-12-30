package chappie.theboys.client.render;

import chappie.theboys.common.entities.TrailEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class TrailRenderer extends EntityRenderer<TrailEntity> {

    public TrailRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void render(TrailEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        if (entityIn.player == null || Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180.0F - entityIn.yBodyRot));
        matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
        matrixStackIn.translate(0.0D, -1.501F, 0.5);
        matrixStackIn.translate((entityIn.level.random.nextFloat() - 1F) / 80, 0, (entityIn.level.random.nextFloat() - 1F) / 80);
        entityIn.model.renderToBuffer(matrixStackIn, bufferIn.getBuffer(RenderType.lightning()), packedLightIn, OverlayTexture.NO_OVERLAY, entityIn.color.getRed() /255F, entityIn.color.getGreen() /255F, entityIn.color.getBlue() /255F, 1F - (entityIn.tickCount / (float) entityIn.lifeTime));
        matrixStackIn.popPose();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(TrailEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
