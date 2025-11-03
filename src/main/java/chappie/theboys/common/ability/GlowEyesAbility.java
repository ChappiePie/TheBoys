package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.AbilityClientProperties;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.model.ModelProperties;
import chappie.theboys.TheBoys;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.util.TBCommonUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GlowEyesAbility extends Ability implements IHasTimer {

    public static final ResourceLocation GLOW_EYES = TheBoys.id("textures/models/glow_eyes.png");

    public Timer eyesTimer = new Timer(() -> 4, this::isEnabled);

    public GlowEyesAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(TBCommonUtil.COLOR, Color.RED);
    }

    @Override
    public void initializeClient(Consumer<AbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new AbilityClientProperties() {

            private final Supplier<HumanoidModel<?>> copyModel = () ->
                    new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER));

            @Override
            public void render(LivingEntityRenderer<? extends LivingEntity, ? extends LivingEntityRenderState, ? extends EntityModel<?>> renderer, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLightIn, LivingEntity entity, ModelProperties modelProperties) {
                if (!modelProperties.root().hasChild("head")) return;
                Color color = dataManager.get(TBCommonUtil.COLOR);
                float red = color.getRed() / 255F, green = color.getGreen() / 255F, blue = color.getBlue() / 255F;
                boolean humanoid = renderer.getModel() instanceof HumanoidModel || renderer.getModel() instanceof ArmedModel && renderer.getModel() instanceof HeadedModel;
                poseStack.pushPose();
                // Basically that's render of eyes without lasers
                {
                    float f = 1.03125f;
                    float alpha = eyesTimer.value(modelProperties.partialTicks());
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
                    nodeCollector.submitCustomGeometry(poseStack, RenderType.beaconBeam(GLOW_EYES, true), (pose, consumer) -> {
                        PoseStack renderStack = new PoseStack();
                        renderStack.last().pose().set(pose.pose());
                        renderStack.last().normal().set(pose.normal());
                        for (int i = 0; i < 3; i++) {
                            renderStack.pushPose();
                            renderStack.translate(0, (i == 2 ? -1 : i) / 32F, 0);
                            this.copyModel.get().head.render(renderStack, consumer, packedLightIn, OverlayTexture.NO_OVERLAY, ARGB.colorFromFloat(i == 0 ? alpha : alpha * 0.25F, red, green, blue));
                            renderStack.popPose();
                        }
                        renderStack.translate(0, 0, -(Math.cos(entity.tickCount * entity.tickCount) / 100F));
                        this.copyModel.get().hat.render(renderStack, consumer, packedLightIn, OverlayTexture.NO_OVERLAY, ARGB.colorFromFloat(alpha, red, green, blue));
                    });
                    poseStack.popPose();
                }
                poseStack.popPose();
            }
        });
    }

    @Override
    public Iterable<Timer> timers() {
        return List.of(this.eyesTimer);
    }
}
