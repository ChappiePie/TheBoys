package chappie.theboys.client.render;

import chappie.theboys.common.entities.TrailEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TrailRenderer extends EntityRenderer<TrailEntity> {
    public static Map<UUID, List<TrailEntity>> WITH_TRAIL = Maps.newHashMap();

    public TrailRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(TrailEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        if (entityIn.player == null) return;
        if (entityIn.renderer == null) {
            boolean contain = WITH_TRAIL.containsKey(entityIn.player.getUUID());
            if (!contain) {
                WITH_TRAIL.put(entityIn.player.getUUID(), Lists.newArrayList(entityIn));
            } else if (contain && WITH_TRAIL.get(entityIn.player.getUUID()) != null && !WITH_TRAIL.get(entityIn.player.getUUID()).contains(entityIn)) {
                WITH_TRAIL.get(entityIn.player.getUUID()).add(entityIn);
            }
            return;
        }
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(180));
        matrixStackIn.translate(0, -1.4F, 0);
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(entityIn.player.yBodyRot));
        matrixStackIn.translate((entityIn.level.random.nextFloat() - 1F) / 80, 0, (entityIn.level.random.nextFloat() - 1F) / 80);
        matrixStackIn.translate(0,0,1);
        entityIn.renderer.getModel().renderToBuffer(matrixStackIn, bufferIn.getBuffer(RenderType.lightning()), packedLightIn, OverlayTexture.NO_OVERLAY, entityIn.color.getRed() /255F, entityIn.color.getGreen() /255F, entityIn.color.getBlue() /255F, 1F - (entityIn.tickCount / entityIn.lifeTime));
        matrixStackIn.popPose();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(TrailEntity entity) {
        return PlayerContainer.BLOCK_ATLAS;
    }

    public static void renderTrail(PlayerRenderer renderer, PlayerEntity player, Color color) {
        if (WITH_TRAIL.containsKey(player.getUUID())) {
            List<TrailEntity> list = WITH_TRAIL.get(player.getUUID());
            if (list == null || list.isEmpty()) {
                WITH_TRAIL.remove(player.getUUID());
                return;
            }
            list.forEach(e -> {
                e.renderer = renderer;
                e.color = color;
            });
            WITH_TRAIL.remove(player.getUUID());
        }
    }
}
