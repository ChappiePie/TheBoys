package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.AbilityClientProperties;
import chappie.modulus.common.ability.base.condition.KeyCondition;
import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.data.DataAccessor;
import chappie.modulus.util.model.ModelProperties;
import chappie.theboys.common.ability.base.TBAbilityTypes;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.particle.LaserParticleOptions;
import chappie.theboys.util.TBCommonUtil;
import chappie.theboys.util.TBConfig;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;

import java.awt.*;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class HeatVisionAbility extends GlowEyesAbility {

    public static final DataAccessor<Integer> MAX_TIMER = new DataAccessor<>("max_timer", DataAccessor.DataSerializer.INT);
    public static final DataAccessor<Float> DISTANCE = new DataAccessor<>("distance", DataAccessor.DataSerializer.FLOAT);
    public static final DataAccessor<Float> STRENGTH = new DataAccessor<>("strength", DataAccessor.DataSerializer.FLOAT);

    public final Timer timer = new Timer(() -> this.dataManager.get(MAX_TIMER), this::isEnabled);
    private Map.Entry<BlockPos, Integer> blocksInFire;
    private Map.Entry<BlockPos, Float> miningProgress;

    public HeatVisionAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
        this.eyesTimer = new Timer(() -> 4, () -> !(this.entity instanceof Player) && isEnabled() || this.conditionManager.test("eyes"));
    }

    public static AbilityBuilder of(String id, Function<KeyCondition, KeyCondition> consumer, Function<KeyCondition, KeyCondition> additionalConsumer) {
        return AbilityBuilder.of(id, TBAbilityTypes.HEAT_VISION).condition(a -> consumer.apply(new KeyCondition(a) {
            @Override
            public boolean get() {
                if (a.enabledTicks >= a.dataManager.get(HeatVisionAbility.MAX_TIMER)) {
                    if (a.conditionManager.conditionsFor("enabling").stream().noneMatch(enabling -> enabling != this && !enabling.get())) {
                        return true;
                    }
                }
                return super.get();
            }
        }), "enabling", "eyes").condition(a -> additionalConsumer.apply(new KeyCondition(a)), "enabling");
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(MAX_TIMER, 4);
        this.dataManager.define(DISTANCE, 20.0F).define(STRENGTH, 1.0F);
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        if (!entity.level().isClientSide()) {
            float configDistance = TBConfig.COMMON.heatVisionRange.get().floatValue();
            if (this.dataManager.get(DISTANCE) != configDistance) {
                this.dataManager.set(DISTANCE, configDistance);
            }
            float configDamage = TBConfig.COMMON.heatVisionDamage.get().floatValue();
            if (this.dataManager.get(STRENGTH) != configDamage / 2F) {
                this.dataManager.set(STRENGTH, configDamage / 2F);
            }
        }
        if (!entity.level().isClientSide() && this.enabledTicks >= this.dataManager.get(MAX_TIMER)) {
            HitResult hitResult = CommonUtil.pick(entity, this.dataManager.get(DISTANCE));
            if (hitResult.getType() != HitResult.Type.MISS) {
                if (hitResult instanceof EntityHitResult rtr && rtr.getEntity() != entity) {
                    this.onHitEntity(rtr);
                } else if (hitResult instanceof BlockHitResult rtr) {
                    this.onHitBlock(rtr);
                }
                CommonUtil.spawnParticleForAll(this.entity.level(),
                        new LaserParticleOptions(this.entity.getId(), Color.WHITE.getRGB()),
                        true, hitResult.getLocation(),
                        new Vec3(entity.getRandom().nextGaussian() * 0.0005D, entity.getRandom().nextGaussian() * 0.0005D, entity.getRandom().nextGaussian() * 0.0005D),
                        0.01F, 1);
                CommonUtil.spawnParticleForAll(this.entity.level(),
                        new LaserParticleOptions(this.entity.getId(), this.dataManager.get(TBCommonUtil.COLOR).getRGB()),
                        true, hitResult.getLocation(),
                        new Vec3(entity.getRandom().nextGaussian() * 0.0005D, entity.getRandom().nextGaussian() * 0.0005D, entity.getRandom().nextGaussian() * 0.0005D),
                        0.04F, 4);
                CommonUtil.spawnParticleForAll(this.entity.level(),
                        ParticleTypes.SMOKE,
                        true, hitResult.getLocation(),
                        new Vec3(entity.getRandom().nextGaussian() * 0.0005D, entity.getRandom().nextGaussian() * 0.0005D, entity.getRandom().nextGaussian() * 0.0005D),
                        0.03F, 10);
            }
        }
    }

    @Override
    public void initializeClient(Consumer<AbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new AbilityClientProperties() {
            @Override
            public void render(LivingEntityRenderer<? extends LivingEntity, ? extends EntityModel<?>> renderer, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, LivingEntity entity, ModelProperties modelProperties) {
                if (!modelProperties.root().hasChild("head")) return;
                Color color = dataManager.get(TBCommonUtil.COLOR);
                float red = color.getRed() / 255F, green = color.getGreen() / 255F, blue = color.getBlue() / 255F;
                boolean humanoid = renderer.getModel() instanceof HumanoidModel;
                poseStack.pushPose();
                HitResult hitResult = CommonUtil.pick(entity, dataManager.get(HeatVisionAbility.DISTANCE));
                double distance = entity.getEyePosition().distanceTo(hitResult.getLocation());

                float f = timer.value(modelProperties.partialTicks());
                if (f != 0) {
                    float y = humanoid ? 0.25F : 0;
                    for (int i = 0; i < 2; i++) {
                        float x = i == 0 ? 0.15F : -0.15F;
                        AABB box = new AABB(x, -y, -0.25F, 0, -y, -0.25F + -distance * f).inflate(0.03D);
                        poseStack.pushPose();
                        modelProperties.root().getChild("head").translateAndRotate(poseStack);
                        TheBoysCap cap = TheBoysCap.getCap(entity);
                        if (cap != null) {
                            float f2 = cap.eyesHeight() - 5;
                            poseStack.translate(0, f2 * 0.0625F, 0);
                            float f1 = cap.eyesLength();
                            float f3 = f1 == 1 ? 0 : f1 == 2 ? 0.0625F * 4F : 0.0625F * (8.25F - (3 - f1) * 4.25F);
                            poseStack.translate(0F, f3, 0F);
                            poseStack.scale(1F, f1, 1F);
                        }
                        poseStack.scale(0.5F, 0.75F, 1);
                        poseStack.translate(x, -0.05, 0);
                        ClientUtil.renderFilledBox(poseStack, bufferIn.getBuffer(ClientUtil.ModRenderTypes.MAIN_LASER), box, 1F, 1F, 1F, f, packedLightIn);
                        VertexConsumer vertexConsumer = bufferIn.getBuffer(ClientUtil.ModRenderTypes.LASER);
                        ClientUtil.renderFilledBox(poseStack, vertexConsumer, box.inflate(0.015D), red, green, blue, f * 0.2F, packedLightIn);
                        ClientUtil.renderFilledBox(poseStack, vertexConsumer, box.inflate(0.03D), red, green, blue, f * 0.2F, packedLightIn);
                        poseStack.popPose();
                    }
                }
                poseStack.popPose();
            }
        });
    }

    protected void onHitEntity(EntityHitResult hitResult) {
        float strength = this.dataManager.get(STRENGTH);
        hitResult.getEntity().setRemainingFireTicks(60 + (int) (strength * 5));
        if (this.entity.level() instanceof ServerLevel serverLevel) {
            hitResult.getEntity().hurt(this.entity.damageSources().mobAttack(entity), strength * 2F);
        }
    }

    protected void onHitBlock(BlockHitResult hitResult) {
        if (TBConfig.COMMON.homelanderBlockDestruction.get()) {
            BlockPos blockPos = hitResult.getBlockPos();
            BlockState blockState = entity.level().getBlockState(blockPos);
            float hardness = blockState.getDestroySpeed(entity.level(), blockPos);

            if (hardness != -1) {
                if (miningProgress == null || !miningProgress.getKey().equals(blockPos)) {
                    miningProgress = new AbstractMap.SimpleEntry<>(blockPos, 0.0f);
                }
                float progress = miningProgress.getValue();
                progress += (float) (TBConfig.COMMON.homelanderMiningSpeedMultiplier.get() / (hardness * 30.0f));
                miningProgress.setValue(progress);

                if (progress >= 1.0f) {
                    if (TBConfig.COMMON.homelander3x3Destruction.get()) {
                        destroy3x3(blockPos, hitResult.getDirection());
                    } else {
                        entity.level().destroyBlock(blockPos, true, entity);
                    }
                    miningProgress = null;
                }
            }
        } else {
            BlockPos blockPos = hitResult.getBlockPos();
            if (this.entity.level().getBlockState(blockPos).getBlock() == Blocks.SAND) {
                if (this.blocksInFire == null || !this.blocksInFire.getKey().equals(blockPos)) {
                    this.blocksInFire = new AbstractMap.SimpleEntry<>(blockPos, 0);
                }
                this.blocksInFire.setValue(this.blocksInFire.getValue() + 1);
                if (this.blocksInFire.getValue() > 60) {
                    this.entity.level().setBlock(blockPos, Blocks.GLASS.defaultBlockState(), 11);
                    this.blocksInFire = null;
                }
            } else {
                blockPos = blockPos.relative(hitResult.getDirection());
                if (this.entity.level().isEmptyBlock(blockPos)) {
                    if (this.blocksInFire == null || !this.blocksInFire.getKey().equals(blockPos)) {
                        this.blocksInFire = new AbstractMap.SimpleEntry<>(blockPos, 0);
                    }
                    this.blocksInFire.setValue(this.blocksInFire.getValue() + 1);
                    if (this.blocksInFire.getValue() > 3) {
                        this.entity.level().setBlock(blockPos, Blocks.FIRE.defaultBlockState(), 11);
                        this.blocksInFire = null;
                    }
                }
            }
        }
    }

    private void destroy3x3(BlockPos center, Direction direction) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (direction.getAxis() == Direction.Axis.X) {
                        entity.level().destroyBlock(center.offset(0, y, z), true, entity);
                    } else if (direction.getAxis() == Direction.Axis.Y) {
                        entity.level().destroyBlock(center.offset(x, 0, z), true, entity);
                    } else {
                        entity.level().destroyBlock(center.offset(x, y, 0), true, entity);
                    }
                }
            }
        }
    }

    @Override
    public Iterable<Timer> timers() {
        return Iterables.concat(super.timers(), List.of(this.timer));
    }
}
