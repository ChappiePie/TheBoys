package chappie.theboys.common.ability;

import chappie.modulus.common.ability.GlowAbility;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.AbilityClientProperties;
import chappie.modulus.util.data.CommonAccessors;
import chappie.modulus.util.model.ModelProperties;
import chappie.theboys.TheBoys;
import chappie.theboys.common.capability.TheBoysCap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;

import java.awt.*;
import java.util.function.Consumer;

/**
 * The Boys-specific glow eyes with multi-layer rendering and eye height/length customization.
 */
public class GlowEyesAbility extends GlowAbility {

    public static final Identifier GLOW_EYES = TheBoys.id("textures/models/glow_eyes.png");

    public GlowEyesAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    protected Identifier getGlowTexture() {
        return GLOW_EYES;
    }

    @Override
    public void initializeClient(Consumer<AbilityClientProperties> consumer) {
        // Skip Core's default single-pass renderer, provide our own multi-layer one
        consumer.accept(new AbilityClientProperties() {

            private HumanoidModel<?> cachedModel;

            private HumanoidModel<?> getModel() {
                if (cachedModel == null) {
                    cachedModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER));
                }
                return cachedModel;
            }

            @Override
            public void render(LivingEntityRenderer<? extends LivingEntity, ? extends LivingEntityRenderState, ? extends EntityModel<?>> renderer, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLightIn, LivingEntity entity, ModelProperties modelProperties) {
                if (!modelProperties.root().hasChild("head")) return;
                Color color = dataManager.get(CommonAccessors.COLOR);
                float red = color.getRed() / 255F, green = color.getGreen() / 255F, blue = color.getBlue() / 255F;
                boolean humanoid = renderer.getModel() instanceof HumanoidModel || renderer.getModel() instanceof ArmedModel && renderer.getModel() instanceof HeadedModel;
                poseStack.pushPose();
                {
                    float f = 1.03125f;
                    float alpha = glowTimer.value(modelProperties.partialTicks());
                    poseStack.pushPose();
                    modelProperties.root().getChild("head").translateAndRotate(poseStack);
                    if (!humanoid) {
                        poseStack.translate(0, 0.19, -0.26F);
                    }
                    TheBoysCap cap = TheBoysCap.getCap(entity);
                    if (cap != null) {
                        float f2 = cap.eyesHeight() - 5;
                        poseStack.translate(0, f2 * 0.0625F, 0);
                        float f1 = cap.eyesLength();
                        float f3 = f1 == 1 ? 0 : f1 == 2 ? 0.0625F * 4F : 0.0625F * (8.25F - (3 - f1) * 4.25F);
                        poseStack.translate(0F, f3, 0F);
                        poseStack.scale(1F, f1, 1F);
                    }
                    poseStack.scale(f, f, f);
                    nodeCollector.submitCustomGeometry(poseStack, net.minecraft.client.renderer.rendertype.RenderTypes.beaconBeam(GLOW_EYES, true), (pose, consumer) -> {
                        PoseStack renderStack = new PoseStack();
                        renderStack.last().pose().set(pose.pose());
                        renderStack.last().normal().set(pose.normal());
                        for (int i = 0; i < 3; i++) {
                            renderStack.pushPose();
                            renderStack.translate(0, (i == 2 ? -1 : i) / 32F, 0);
                            this.getModel().head.render(renderStack, consumer, packedLightIn, OverlayTexture.NO_OVERLAY, ARGB.colorFromFloat(i == 0 ? alpha : alpha * 0.25F, red, green, blue));
                            renderStack.popPose();
                        }
                        renderStack.translate(0, 0, -(Math.cos(entity.tickCount * entity.tickCount) / 100F));
                        this.getModel().hat.render(renderStack, consumer, packedLightIn, OverlayTexture.NO_OVERLAY, ARGB.colorFromFloat(alpha, red, green, blue));
                    });
                    poseStack.popPose();
                }
                poseStack.popPose();
            }
        });
    }
}