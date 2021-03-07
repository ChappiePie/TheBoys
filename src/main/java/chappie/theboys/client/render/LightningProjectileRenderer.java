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
import net.minecraft.util.math.AxisAlignedBB;
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
        float r = 0.15F;
        AxisAlignedBB box = new AxisAlignedBB(-r, -r, -r, r, r, r);
        matrixStackIn.push();
        for (int i = 0; i < 5; i++) {
            float angle = entityIn.ticksExisted * 4 + i * 180;
            matrixStackIn.rotate(new Quaternion(angle, -angle, angle, true));
            HUClientUtil.renderFilledBox(matrixStackIn, bufferIn.getBuffer(HUClientUtil.HURenderTypes.LASER), box, 0f, 1f, 0f, 0.5f, packedLightIn);
            for (int j = 0; j < 5; j++) {
                float angleJ = entityIn.ticksExisted * 4 + j * 180;
                matrixStackIn.rotate(new Quaternion(angleJ, -angleJ, angleJ, true));
                HUClientUtil.renderFilledBox(matrixStackIn, bufferIn.getBuffer(HUClientUtil.HURenderTypes.LASER), box.shrink(0.025F), 1f, 1f, 1f, 1f, packedLightIn);
            }
        }
        matrixStackIn.pop();
    }

    @Override
    public ResourceLocation getEntityTexture(LightningProjectile entity) {
        return PlayerContainer.LOCATION_BLOCKS_TEXTURE;
    }
}
