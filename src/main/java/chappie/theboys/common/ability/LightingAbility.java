package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.AbilityClientProperties;
import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.model.ModelProperties;
import chappie.theboys.TheBoys;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class LightingAbility extends Ability implements IHasTimer {
    public static final ResourceLocation WHITE = new ResourceLocation(TheBoys.MODID, "textures/models/white.png");

    public Timer timer = new Timer(() -> 10, this::isEnabled);

    public LightingAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void initializeClient(Consumer<AbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new AbilityClientProperties() {

            private List<Vec3> beamVectors;

            public void generateLightningBeams() {
                Random random = new Random();
                beamVectors = new ArrayList<>();
                Vec3 coreStart = new Vec3(0, 0, 0);
                int coreLength = random.nextInt(3) + 7;
                for (int core = 0; core < coreLength; core++) {
                    Vec3 coreEnd = coreStart.add(0, 0, 1).add(randomVector(random, 0.3f).multiply(2.5, 2.5, 2.5));
                    beamVectors.add(coreStart);
                    beamVectors.add(coreEnd);
                    coreStart = coreEnd;

                    beamVectors.addAll(generateBranch(random, coreEnd, 1, 0.5f, 1));
                }
            }

            private List<Vec3> generateBranch(Random random, Vec3 origin, int maxLength, float splitChance, int recursionCount) {
                List<Vec3> branchSegments = new ArrayList<>();
                Vec3 branchStart = origin;
                int branches = random.nextInt(maxLength + 1);
                int dir = random.nextBoolean() ? 1 : -1;
                float branchLength = .75f / (recursionCount + 1);
                for (int i = 0; i < branches; i++) {
                    Vec3 branchEnd = branchStart.add(dir * branchLength, 0, branchLength).add(randomVector(random, 0.3f));
                    branchSegments.add(branchStart);
                    branchSegments.add(branchEnd);
                    if (random.nextFloat() <= splitChance)
                        branchSegments.addAll(generateBranch(random, branchEnd, maxLength - 1, splitChance * 1.2f, recursionCount + 1));
                    branchStart = branchEnd;
                }
                return branchSegments;
            }

            private Vec3 randomVector(Random random, float radius) {
                double x = random.nextDouble() * 2 * radius - radius;
                double y = random.nextDouble() * 2 * radius - radius;
                double z = random.nextDouble() * 2 * radius - radius;
                return new Vec3(x, y, z);
            }

            public List<Vec3> getBeamCache() {
                if (beamVectors == null)
                    generateLightningBeams();
                return beamVectors;
            }

            @Override
            public void render(LivingEntityRenderer<? extends LivingEntity, ? extends EntityModel<?>> renderer, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, LivingEntity entity, ModelProperties modelProperties) {
                if (!(renderer.getModel() instanceof HumanoidModel model)) return;
                for (int k = 0; k < 2; k++) {
                    poseStack.pushPose();

                    //VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
                    model.translateToHand(k == 0 ? HumanoidArm.RIGHT : HumanoidArm.LEFT, poseStack);
                    if (k == 0) {
                        poseStack.mulPose(Axis.YP.rotationDegrees(180F));
                    }

                    poseStack.mulPose(Axis.XP.rotationDegrees(-90F));
                    poseStack.translate(0.075, 0, 0.6);

                    if (entity.tickCount % 2 == 0 && !Minecraft.getInstance().isPaused())
                        generateLightningBeams();
                    List<Vec3> segments = getBeamCache();

                    //TheBoys.LOGGER.debug("ElectrocuteRenderer.segments.length: {}",segments.size());

                    float f = timer.value(modelProperties.partialTicks());
                    float width = 0.05f;
                    float height = width;
                    for (int i = 0; i < segments.size() - 1; i += 2) {
                        var from = segments.get(i);
                        var to = segments.get(i + 1);
                        Color c = new Color(0, 156, 255);
                        Color c1 = new Color(0, 226, 255);
                        renderPart(from, to, width, height, poseStack, bufferIn.getBuffer(ClientUtil.ModRenderTypes.glow(WHITE)), 1, 1, 1, f);
                        VertexConsumer consumer = bufferIn.getBuffer(RenderType.entityTranslucentEmissive(WHITE));
                        renderPart(from, to, width + 0.1F, height + 0.1F, poseStack, consumer, c1.getRed() / 255F, c1.getGreen() / 255F, c1.getBlue() / 255F, 0.1F * f);
                        renderPart(from, to, width + 0.2F, height + 0.2F, poseStack, consumer, c.getRed() / 255F, c.getGreen() / 255F, c.getBlue() / 255F, 0.1F * f);
                    }
                    poseStack.popPose();
                }
            }

            private void renderPart(Vec3 from, Vec3 to, float width, float height, PoseStack poseStack, VertexConsumer consumer, float r, float g, float b, float a) {
                //Bottom
                renderQuad(from.subtract(0, height * .5f, 0), to.subtract(0, height * .5f, 0), width, 0, poseStack, consumer, r, g, b, a);
                //Top
                renderQuad(from.add(0, height * .5f, 0), to.add(0, height * .5f, 0), width, 0, poseStack, consumer, r, g, b, a);
                //Left
                renderQuad(from.subtract(width * .5f, 0, 0), to.subtract(width * .5f, 0, 0), 0, height, poseStack, consumer, r, g, b, a);
                //Right
                renderQuad(from.add(width * .5f, 0, 0), to.add(width * .5f, 0, 0), 0, height, poseStack, consumer, r, g, b, a);
            }

            private void renderQuad(Vec3 from, Vec3 to, float width, float height, PoseStack poseStack, VertexConsumer consumer, float r, float g, float b, float a) {
                Matrix4f poseMatrix = poseStack.last().pose();
                Matrix3f normalMatrix = poseStack.last().normal();

                float halfWidth = width * 0.5f;
                float halfHeight = height * 0.5f;
                consumer.vertex(poseMatrix, (float) from.x - halfWidth, (float) from.y - halfHeight, (float) from.z).color(r, g, b, a).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
                consumer.vertex(poseMatrix, (float) from.x + halfWidth, (float) from.y + halfHeight, (float) from.z).color(r, g, b, a).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
                consumer.vertex(poseMatrix, (float) to.x + halfWidth, (float) to.y + halfHeight, (float) to.z).color(r, g, b, a).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
                consumer.vertex(poseMatrix, (float) to.x - halfWidth, (float) to.y - halfHeight, (float) to.z).color(r, g, b, a).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            }
        });
    }

    @Override
    public List<Timer> timers() {
        return List.of(this.timer);
    }
}
