package chappie.theboys.client.render;

import chappie.theboys.common.entities.LightningProjectile;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Quaternion;
import xyz.heroesunited.heroesunited.util.HUClientUtil;

public class LightningProjectileRenderer extends EntityRenderer<LightningProjectile> {

    public LightningProjectileRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(LightningProjectile entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        float r = 0.15F;
        AxisAlignedBB box = new AxisAlignedBB(-r, -r, -r, r, r, r);
        matrixStackIn.pushPose();
        for (int i = 0; i < 5; i++) {
            float angle = entityIn.tickCount * 4 + i * 180;
            matrixStackIn.mulPose(new Quaternion(angle, -angle, angle, true));
            HUClientUtil.renderFilledBox(matrixStackIn, bufferIn.getBuffer(HUClientUtil.HURenderTypes.LASER), box, 0f, 1f, 0f, 0.5f, packedLightIn);
            for (int j = 0; j < 5; j++) {
                float angleJ = entityIn.tickCount * 4 + j * 180;
                matrixStackIn.mulPose(new Quaternion(angleJ, -angleJ, angleJ, true));
                HUClientUtil.renderFilledBox(matrixStackIn, bufferIn.getBuffer(HUClientUtil.HURenderTypes.LASER), box.deflate(0.025F), 1f, 1f, 1f, 1f, packedLightIn);
            }
        }
        matrixStackIn.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(LightningProjectile entity) {
        return PlayerContainer.BLOCK_ATLAS;
    }
}
