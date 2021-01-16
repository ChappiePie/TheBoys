package chappie.theboys.client.render;

import chappie.theboys.common.entities.LightningProjectile;
import chappie.theboys.common.entities.TrailEntity;
import chappie.theboys.util.TBClientUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import xyz.heroesunited.heroesunited.util.HUClientUtil;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LightningProjectileRenderer extends EntityRenderer<LightningProjectile> {

    public LightningProjectileRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(LightningProjectile entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        if (entityIn.ticksExisted >= 2 || !(this.renderManager.info.getRenderViewEntity().getDistanceSq(entityIn) < 12.25D)) {
            matrixStackIn.push();
            matrixStackIn.rotate(this.renderManager.getCameraOrientation());
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F));
            for (int i = 0; i < 3; i++) {
                matrixStackIn.push();
                matrixStackIn.scale(0.05F, 0.05F, 0.05F);
                matrixStackIn.translate(i, 10, 0);
                matrixStackIn.rotate(new Quaternion(90,0,0, true));
                HUClientUtil.renderLightning(entityIn.world.rand, matrixStackIn, bufferIn, packedLightIn, 2, i, entityIn.getColor());
                matrixStackIn.pop();
            }
            matrixStackIn.pop();
            super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        }
    }

    @Override
    public ResourceLocation getEntityTexture(LightningProjectile entity) {
        return PlayerContainer.LOCATION_BLOCKS_TEXTURE;
    }
}
