package chappie.theboys.client.render;

import chappie.theboys.common.entities.TrailEntity;
import chappie.theboys.util.TBClientUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TrailRenderer extends EntityRenderer<TrailEntity> {
    public static Map<UUID, List<TrailEntity>> TO_EDIT = Maps.newHashMap();

    public TrailRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(TrailEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        if (entityIn.parent == null)
            return;

        if (entityIn.renderer == null) {
            if (!TO_EDIT.containsKey(entityIn.parent.getUniqueID())) {
                TO_EDIT.put(entityIn.parent.getUniqueID(), Lists.newArrayList(entityIn));
            } else if (TO_EDIT.containsKey(entityIn.parent.getUniqueID()) && TO_EDIT.get(entityIn.parent.getUniqueID()) != null && !TO_EDIT.get(entityIn.parent.getUniqueID()).contains(entityIn)) {
                TO_EDIT.get(entityIn.parent.getUniqueID()).add(entityIn);
            }

            return;
        }

        matrixStackIn.push();
        float alpha = 1F - ((float) entityIn.ticksExisted / (float) entityIn.lifeTime);
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180));
        matrixStackIn.translate(0, -1.4F, 0);
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(entityIn.renderYawOffset));
        for (int i = 0; i < 10; i++) {
            matrixStackIn.translate((entityIn.world.rand.nextFloat() - 1F) / 80, 0, (entityIn.world.rand.nextFloat() - 1F) / 80);
            if (i == 10) i = 0;
        }
        matrixStackIn.translate(0,0,0.5);
        entityIn.renderer.getEntityModel().render(matrixStackIn, bufferIn.getBuffer(TBClientUtil.TBRenderTypes.TRAIL), packedLightIn, OverlayTexture.NO_OVERLAY, entityIn.red, entityIn.green, entityIn.blue, alpha / 1.4F);
        matrixStackIn.pop();

        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getEntityTexture(TrailEntity entity) {
        return PlayerContainer.LOCATION_BLOCKS_TEXTURE;
    }


    public static void renderTrail(PlayerRenderer renderer, PlayerEntity player, float red, float green, float blue) {
        if (TO_EDIT.containsKey(player.getUniqueID())) {
            List<TrailEntity> list = TO_EDIT.get(player.getUniqueID());
            if (list == null || list.isEmpty()) {
                TO_EDIT.remove(player.getUniqueID());
                return;
            }
            list.forEach(e -> {
                e.renderer = renderer;
                e.red = red;
                e.green = green;
                e.blue = blue;
                e.renderYawOffset = player.renderYawOffset;
            });
            TO_EDIT.remove(player.getUniqueID());
        }
    }
}
