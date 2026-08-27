package chappie.theboys.client.gui.render.pip;

import chappie.theboys.client.gui.render.state.LaserPreviewRenderState;
import chappie.theboys.client.renderer.LaserRenderTypes;
import chappie.theboys.common.ability.HeatVisionAbility;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4fStack;

import java.util.function.Supplier;

public class GuiLaserRenderer extends PictureInPictureRenderer<LaserPreviewRenderState> {

    private static final Supplier<PlayerModel> EYES_LAYER_MODEL = () -> new PlayerModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER), false);

    public GuiLaserRenderer() {
        super();
    }

    @Override
    public Class<LaserPreviewRenderState> getRenderStateClass() {
        return LaserPreviewRenderState.class;
    }

    @Override
    protected void renderToTexture(LaserPreviewRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gameRenderer.lighting().setupFor(Lighting.Entry.PLAYER_SKIN);
        Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
        int guiScale = minecraft.getWindow().getGuiScale();
        matrixStack.pushMatrix();
        float scaled = guiScale * state.scale();
        matrixStack.rotateAround(Axis.XP.rotationDegrees(state.rotationX()), 0.0F, scaled * -state.pivotY(), 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.rotationY()));
        poseStack.translate(0.0F, -1.6010001F, 0.0F);

        // Render Player model
        RenderType renderType = state.model().renderType(state.texture());
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
            PoseStack renderStack = new PoseStack();
            renderStack.last().pose().set(pose.pose());
            renderStack.last().normal().set(pose.normal());
            state.model().renderToBuffer(renderStack, consumer, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
        });

        // Render Lasers and eyes overlay
        poseStack.pushPose();
        state.model().head.translateAndRotate(poseStack);
        poseStack.pushPose();
        poseStack.translate(0, (state.eyesHeight() - 5) * 0.0625F, 0);
        float length = state.eyesLength();
        float offset = length == 1 ? 0 : length == 2 ? 0.0625F * 4F : 0.0625F * (8.25F - (3 - length) * 4.25F);
        poseStack.translate(0F, offset, 0F);
        poseStack.scale(1F, length, 1F);
        renderEyes(poseStack, submitNodeCollector, state.tickCount());
        renderBeams(poseStack, submitNodeCollector, state.laserLength());
        poseStack.popPose();
        poseStack.popPose();

        minecraft.gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_3D);
        matrixStack.popMatrix();
    }

    private void renderEyes(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int tickCount) {
        poseStack.pushPose();
        poseStack.scale(1.03125F, 1.03125F, 1.03125F);
        RenderType beaconBeam = RenderTypes.beaconBeam(HeatVisionAbility.GLOW_EYES, true);
        for (int i = 0; i < 3; i++) {
            poseStack.pushPose();
            poseStack.translate(0, (i == 2 ? -1 : i) / 32F, 0);
            float alpha = i == 0 ? 1F : 0.25F;
            int color = ARGB.colorFromFloat(alpha, 1.0F, 0.0F, 0.0F);
            submitNodeCollector.submitCustomGeometry(poseStack, beaconBeam, (pose, consumer) -> {
                PoseStack rs = new PoseStack();
                rs.last().pose().set(pose.pose());
                rs.last().normal().set(pose.normal());
                EYES_LAYER_MODEL.get().head.render(rs, consumer, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color);
            });
            poseStack.popPose();
        }
        poseStack.translate(0, 0, -(Math.cos(tickCount * tickCount) / 100F));
        int hatColor = ARGB.colorFromFloat(1F, 1.0F, 0.0F, 0.0F);
        submitNodeCollector.submitCustomGeometry(poseStack, beaconBeam, (pose, consumer) -> {
            PoseStack rs = new PoseStack();
            rs.last().pose().set(pose.pose());
            rs.last().normal().set(pose.normal());
            EYES_LAYER_MODEL.get().hat.render(rs, consumer, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, hatColor);
        });
        poseStack.popPose();
    }

    private void renderBeams(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, double length) {
        if (length == 0) {
            return;
        }
        for (int i = 0; i < 2; i++) {
            float x = i == 0 ? 0.15F : -0.15F;
            AABB box = new AABB(x, -0.25F, -0.25F, 0, -0.25F, -length).inflate(0.03D);
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.75F, 1);
            poseStack.translate(x, -0.05, 0);

            // Core (translucent, white) — no depth test for PIP
            submitNodeCollector.submitCustomGeometry(poseStack, LaserRenderTypes.PIP_LASER_CORE, (pose, consumer) ->
                    LaserRenderTypes.renderLaserBox(pose.pose(), consumer, box, 1F, 1F, 1F, 1F));
            // Glow (additive, colored) — no depth test for PIP
            AABB glowBox1 = box.inflate(0.035D);
            AABB glowBox2 = box.inflate(0.05D);
            submitNodeCollector.submitCustomGeometry(poseStack, LaserRenderTypes.PIP_LASER_GLOW, (pose, consumer) -> {
                LaserRenderTypes.renderLaserBox(pose.pose(), consumer, glowBox1, 1F, 0F, 0F, 0.2F);
                LaserRenderTypes.renderLaserBox(pose.pose(), consumer, glowBox2, 1F, 0F, 0F, 0.2F);
            });

            poseStack.popPose();
        }
    }

    @Override
    protected String getTextureLabel() {
        return "laser preview";
    }
}
