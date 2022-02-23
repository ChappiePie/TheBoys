package chappie.theboys.client.render;

import chappie.theboys.common.entities.LightningProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import xyz.heroesunited.heroesunited.util.HUClientUtil;

public class LightningProjectileRenderer extends EntityRenderer<LightningProjectile> {

    public LightningProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void render(LightningProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn) {
        if (entity.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 6.125D)) {
            poseStack.pushPose();
            poseStack.scale(0.05F, 0.06F, 0.05F);
            poseStack.translate(0, 10, 0);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 90.0F));
            poseStack.translate(0.25, -0.5, 0);
            if (entity.getLightningType() != null && entity.getLightningType().equals(LightningProjectile.Type.STARLIGHT)) {
                float r = 2F;
                AABB box = new AABB(-r, -r, -r, r, r, r);
                HUClientUtil.renderAura(poseStack, bufferIn.getBuffer(RenderType.lightning()), box, 0.5F, entity.getColor(), packedLightIn, entity.tickCount);
            } else {
                HUClientUtil.renderLightning(entity.level.random, poseStack, bufferIn, packedLightIn, 5, 1, entity.getColor());
            }
            poseStack.popPose();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(LightningProjectile entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
