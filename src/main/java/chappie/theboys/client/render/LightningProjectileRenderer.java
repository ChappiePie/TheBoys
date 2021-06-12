package chappie.theboys.client.render;

import chappie.theboys.common.entities.LightningProjectile;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import xyz.heroesunited.heroesunited.util.HUClientUtil;

public class LightningProjectileRenderer extends EntityRenderer<LightningProjectile> {

    public LightningProjectileRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(LightningProjectile entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int packedLightIn) {
        if (entity.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 6.125D)) {
            matrixStack.pushPose();
            matrixStack.scale(0.05F, 0.06F, 0.05F);
            matrixStack.translate(0, 10, 0);
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot) - 90.0F));
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entity.xRotO, entity.xRot) + 90.0F));
            matrixStack.translate(0.25, -0.5, 0);
            if (entity.getLightningType() != null && entity.getLightningType().equals(LightningProjectile.Type.STARLIGHT)) {
                float r = 2F;
                AxisAlignedBB box = new AxisAlignedBB(-r, -r, -r, r, r, r);
                HUClientUtil.renderAura(matrixStack, bufferIn.getBuffer(RenderType.lightning()), box, 0.5F, entity.getColor(), packedLightIn, entity.tickCount);
            } else {
                HUClientUtil.renderLightning(entity.level.random, matrixStack, bufferIn, packedLightIn, 5, 1, entity.getColor());
            }
            matrixStack.popPose();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(LightningProjectile entity) {
        return PlayerContainer.BLOCK_ATLAS;
    }
}
