package chappie.theboys.client.gui.render.pip;

import chappie.modulus.util.ClientUtil;
import chappie.theboys.client.gui.render.state.LaserPreviewRenderState;
import chappie.theboys.common.ability.HeatVisionAbility;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fStack;

import java.util.function.Supplier;

public class GuiLaserRenderer extends PictureInPictureRenderer<LaserPreviewRenderState> {

    private static final Supplier<PlayerModel> EYES_LAYER_MODEL = () -> new PlayerModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER), false);

    public GuiLaserRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public @NotNull Class<LaserPreviewRenderState> getRenderStateClass() {
        return LaserPreviewRenderState.class;
    }

    @Override
    protected void renderToTexture(LaserPreviewRenderState state, PoseStack poseStack) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.PLAYER_SKIN);
        Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
        int guiScale = minecraft.getWindow().getGuiScale();
        matrixStack.pushMatrix();
        float scaled = guiScale * state.scale();
        matrixStack.rotateAround(Axis.XP.rotationDegrees(state.rotationX()), 0.0F, scaled * -state.pivotY(), 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.rotationY()));
        poseStack.translate(0.0F, -1.6010001F, 0.0F);

        // Render Player model
        RenderType renderType = state.model().renderType(state.texture());
        state.model().renderToBuffer(poseStack, this.bufferSource.getBuffer(renderType), 15728880, OverlayTexture.NO_OVERLAY);

        // Render Lasers and eyes overlay
        poseStack.pushPose();
        state.model().head.translateAndRotate(poseStack);
        poseStack.pushPose();
        poseStack.translate(0, (state.eyesHeight() - 5) * 0.0625F, 0);
        float length = state.eyesLength();
        float offset = length == 1 ? 0 : length == 2 ? 0.0625F * 4F : 0.0625F * (8.25F - (3 - length) * 4.25F);
        poseStack.translate(0F, offset, 0F);
        poseStack.scale(1F, length, 1F);
        renderEyes(poseStack, state.tickCount());
        renderBeams(poseStack, state.laserLength());
        poseStack.popPose();
        poseStack.popPose();

        this.bufferSource.endBatch();
        minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
        matrixStack.popMatrix();
    }

    private void renderEyes(PoseStack poseStack, int tickCount) {
        poseStack.pushPose();
        poseStack.scale(1.03125F, 1.03125F, 1.03125F);
        VertexConsumer vertexConsumer = this.bufferSource.getBuffer(RenderType.beaconBeam(HeatVisionAbility.GLOW_EYES, true));
        for (int i = 0; i < 3; i++) {
            poseStack.pushPose();
            poseStack.translate(0, (i == 2 ? -1 : i) / 32F, 0);
            float alpha = i == 0 ? 1F : 0.25F;
            renderModelPart(EYES_LAYER_MODEL.get().head, poseStack, vertexConsumer, ARGB.colorFromFloat(alpha, 1.0F, 0.0F, 0.0F));
            poseStack.popPose();
        }
        poseStack.translate(0, 0, -(Math.cos(tickCount * tickCount) / 100F));
        renderModelPart(EYES_LAYER_MODEL.get().hat, poseStack, vertexConsumer, ARGB.colorFromFloat(1F, 1.0F, 0.0F, 0.0F));
        poseStack.popPose();
    }

    private void renderBeams(PoseStack poseStack, double length) {
        if (length == 0) {
            return;
        }
        for (int i = 0; i < 2; i++) {
            float x = i == 0 ? 0.15F : -0.15F;
            AABB box = new AABB(x, -0.25F, -0.25F, 0, -0.25F, -length).inflate(0.03D);
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.75F, 1);
            poseStack.translate(x, -0.05, 0);
            ClientUtil.renderFilledBox(poseStack.last().pose(), this.bufferSource.getBuffer(ClientUtil.ModRenderTypes.MAIN_LASER), box, 1F, 1F, 1F, 1, 15728880);
            VertexConsumer laserConsumer = this.bufferSource.getBuffer(ClientUtil.ModRenderTypes.LASER);
            ClientUtil.renderFilledBox(poseStack.last().pose(), laserConsumer, box.inflate(0.015D), 1F, 0F, 0F, 0.2F, 15728880);
            ClientUtil.renderFilledBox(poseStack.last().pose(), laserConsumer, box.inflate(0.03D), 1F, 0F, 0F, 0.2F, 15728880);
            poseStack.popPose();
        }
    }

    private void renderModelPart(ModelPart headPart, PoseStack poseStack, VertexConsumer vertexConsumer, int color) {
        headPart.render(poseStack, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY, color);
    }

    @Override
    protected @NotNull String getTextureLabel() {
        return "laser preview";
    }
}
