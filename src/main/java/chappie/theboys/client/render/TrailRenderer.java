package chappie.theboys.client.render;

import chappie.theboys.common.entities.TrailEntity;
import chappie.theboys.util.TBClientUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
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
            boolean contain = WITH_TRAIL.containsKey(entityIn.player.getUniqueID());
            if (!contain) {
                WITH_TRAIL.put(entityIn.player.getUniqueID(), Lists.newArrayList(entityIn));
            } else if (contain && WITH_TRAIL.get(entityIn.player.getUniqueID()) != null && !WITH_TRAIL.get(entityIn.player.getUniqueID()).contains(entityIn)) {
                WITH_TRAIL.get(entityIn.player.getUniqueID()).add(entityIn);
            }
            return;
        }
        matrixStackIn.push();
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180));
        matrixStackIn.translate(0, -1.4F, 0);
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(entityIn.player.renderYawOffset));
        matrixStackIn.translate((entityIn.world.rand.nextFloat() - 1F) / 80, 0, (entityIn.world.rand.nextFloat() - 1F) / 80);
        matrixStackIn.translate(0,0,1);
        entityIn.renderer.getEntityModel().render(matrixStackIn, bufferIn.getBuffer(RenderType.getLightning()), packedLightIn, OverlayTexture.NO_OVERLAY, entityIn.color.getRed() /255F, entityIn.color.getGreen() /255F, entityIn.color.getBlue() /255F, 1F - (entityIn.ticksExisted / entityIn.lifeTime));
        matrixStackIn.pop();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getEntityTexture(TrailEntity entity) {
        return PlayerContainer.LOCATION_BLOCKS_TEXTURE;
    }

    public static void renderTrail(PlayerRenderer renderer, PlayerEntity player, Color color) {
        if (WITH_TRAIL.containsKey(player.getUniqueID())) {
            List<TrailEntity> list = WITH_TRAIL.get(player.getUniqueID());
            if (list == null || list.isEmpty()) {
                WITH_TRAIL.remove(player.getUniqueID());
                return;
            }
            list.forEach(e -> {
                e.renderer = renderer;
                e.color = color;
            });
            WITH_TRAIL.remove(player.getUniqueID());
        }
    }
}
