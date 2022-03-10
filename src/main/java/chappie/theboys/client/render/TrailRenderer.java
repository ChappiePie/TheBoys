package chappie.theboys.client.render;

import chappie.theboys.common.entities.TrailEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
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

    public static class MyRenderTypes extends RenderType {

        protected static final RenderStateShard.TransparencyStateShard MY_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("my_transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        });

        private static final RenderType TRAIL = create("theboys:trail", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .setTransparencyState(MY_TRANSPARENCY)
                .createCompositeState(false));

        public MyRenderTypes(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
            super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
        }
    }
}
