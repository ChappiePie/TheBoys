package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.AbilityClientProperties;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.data.DataAccessor;
import chappie.modulus.util.events.SetupAnimCallback;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class FlightAbility extends chappie.modulus.common.ability.FlightAbility {

    // TB-specific data
    public static final DataAccessor<Boolean> BREAK_BLOCKS = new DataAccessor<>("break_blocks", DataAccessor.DataSerializer.BOOLEAN);
    public static final DataAccessor<Boolean> BOOSTING = new DataAccessor<>("boosting", DataAccessor.DataSerializer.BOOLEAN);
    public static final DataAccessor<Boolean> ARM_AHEAD = new DataAccessor<>("arm_ahead", DataAccessor.DataSerializer.BOOLEAN);
    /**
     * Block damage map: when a supe lands with enough force, these blocks transform.
     * Add/remove entries to customize environmental destruction.
     */
    private static final java.util.Map<Block, Block> BLOCK_DAMAGE_MAP = java.util.Map.ofEntries(
            java.util.Map.entry(Blocks.STONE, Blocks.COBBLESTONE),
            java.util.Map.entry(Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS),
            java.util.Map.entry(Blocks.COBBLESTONE, Blocks.GRAVEL),
            java.util.Map.entry(Blocks.GRASS_BLOCK, Blocks.DIRT),
            java.util.Map.entry(Blocks.DIRT, Blocks.COARSE_DIRT),
            java.util.Map.entry(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG),
            java.util.Map.entry(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG),
            java.util.Map.entry(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG),
            java.util.Map.entry(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG),
            java.util.Map.entry(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG),
            java.util.Map.entry(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG)
    );

    public FlightAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(BREAK_BLOCKS, true);
        this.dataManager.define(BOOSTING, false, false);
        this.dataManager.define(ARM_AHEAD, false, false);
    }

    @Override
    public void onDataUpdated(DataAccessor<?> accessor) {
        super.onDataUpdated(accessor);
        if (accessor == SPRINTING) {
            if (this.dataManager.get(SPRINTING)) {
                this.dataManager.set(ARM_AHEAD, this.entity.getRandom().nextInt(100) < 5);
            } else {
                this.dataManager.set(ARM_AHEAD, false);
            }
        }
    }

    /**
     * Blocks that get destroyed entirely on impact
     */
    private static final java.util.Set<Block> BLOCK_DESTROY_SET = java.util.Set.of(Blocks.GLASS);
    public final IHasTimer.Cooldown cooldown = addCooldown();

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        if (!enabled) {
            this.dataManager.set(BOOSTING, false);
            this.dataManager.set(SPRINTING, false);
        }
    }

    @Override
    protected float modifySpeed(LivingEntity entity, float baseSpeed, boolean sprinting) {
        if (!sprinting || this.dataManager.get(FORWARD_IMPULSE) <= 0) {
            this.dataManager.set(BOOSTING, false);
        }
        if (sprinting) {
            if (this.cooldown.end()) {
                if (this.conditionManager.test("boost")) {
                    this.cooldown.start(60);
                    this.dataManager.set(BOOSTING, true);
                    CommonUtil.spawnParticleForAll(this.entity.level(), ParticleTypes.EXPLOSION,
                            true, this.entity.position(), Vec3.ZERO, 1, 10);
                }
            }
            if (this.dataManager.get(BOOSTING)) {
                baseSpeed += this.cooldown.value(1) * 4F;
                CommonUtil.spawnParticleForAll(this.entity.level(), ParticleTypes.CLOUD,
                        true, this.entity.position(), Vec3.ZERO, 0.05F, 10);
                if (this.cooldown.timer == 0) {
                    this.dataManager.set(BOOSTING, false);
                }
            }
        } else {
            this.dataManager.set(BOOSTING, false);
        }
        return baseSpeed;
    }

    public boolean causeFallDamage(ServerLevel level, LivingEntity entity, double fallDistance) {
        boolean sprinting = this.dataManager.get(SPRINTING) || entity.isSprinting();
        if (this.dataManager.get(BREAK_BLOCKS) && fallDistance > 20 && (sprinting || !this.isEnabled())) {
            for (int x = 0; x < 5; x++) {
                for (int y = 0; y < 5; y++) {
                    for (int z = 0; z < 5; z++) {
                        double xPos = entity.getX() - 2.5 + x + entity.level().getRandom().nextInt(5);
                        double yPos = entity.getY() - 2.5 + y + entity.level().getRandom().nextInt(5);
                        double zPos = entity.getZ() - 2.5 + z + entity.level().getRandom().nextInt(5);
                        BlockPos pos = new BlockPos((int) xPos, (int) yPos, (int) zPos);
                        Block block = entity.level().getBlockState(pos).getBlock();

                        Block replacement = BLOCK_DAMAGE_MAP.get(block);
                        if (replacement != null) {
                            entity.level().setBlockAndUpdate(pos, replacement.defaultBlockState());
                        } else if (BLOCK_DESTROY_SET.contains(block)) {
                            entity.level().destroyBlock(pos, false);
                        }

                        level.sendParticles(ParticleTypes.EXPLOSION, false, false, entity.getX(), entity.getY() + 0.25F, entity.getZ(), 0, (fallDistance / entity.level().getHeight()) * 10, 0.0D, 0.0D, 1F);
                        entity.playSound(SoundEvents.MOOSHROOM_SHEAR, 1.0F, 1.0F);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void initializeClient(Consumer<AbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new FlightClientProperties(this));
    }

    public record FlightClientProperties(FlightAbility ability) implements AbilityClientProperties {

        @Override
        public void setupAnim(SetupAnimCallback.SetupAnimEvent event) {
            AbilityClientProperties.super.setupAnim(event);
            LivingEntity entity = event.entity();
            var properties = event.modelProperties();
            HumanoidModel<?> model = event.model();
            float partialTicks = properties.partialTicks();
            float flight = this.ease(this.ability.timer.value(partialTicks));
            if (flight <= 0.001F) return;

            float forward = this.ease(this.ability.forwardTimer.value(partialTicks));
            float backward = this.ease(this.ability.backwardTimer.value(partialTicks));
            float sprint = this.ease(this.ability.sprintingTimer.value(partialTicks)) * flight;
            float baseBlend = this.ease(Mth.clamp(1.0F - (forward + backward) / 2.0F, 0.0F, 1.0F));
            float toRad = Mth.DEG_TO_RAD * flight;
            if (!(event.state() instanceof ArmedEntityRenderState state)) return;
            float bob = Mth.sin(state.ageInTicks * 0.067F) * 0.05F * flight;

            boolean right = !entity.isUsingItem() && state.rightArmPose == HumanoidModel.ArmPose.EMPTY && !state.leftArmPose.isTwoHanded();
            boolean left = !entity.isUsingItem() && state.leftArmPose == HumanoidModel.ArmPose.EMPTY && !state.rightArmPose.isTwoHanded();

            model.head.xRot /= 1 + flight;
            model.head.xRot += bob;
            model.body.y -= bob * 4F;
            float relax = (1.0F - sprint / Math.max(flight, 0.001F)) * flight;
            if (right) {
                model.rightArm.xRot += bob * 3.0F * relax;
            }
            if (left) {
                model.leftArm.xRot += bob * 3.0F * relax;
            }
            model.rightLeg.xRot -= model.rightLeg.xRot * flight;
            model.rightLeg.xRot += bob;
            model.leftLeg.xRot -= model.leftLeg.xRot * flight;
            model.leftLeg.xRot -= bob;

            if (baseBlend > 0.001F) {
                this.setupBaseAnim(model, toRad * baseBlend, right, left);
            }
            if (forward > 0.001F) {
                this.setupForwardAnim(model, forward, baseBlend, toRad * forward, right, left);
            }
            if (backward > 0.001F) {
                this.setupBackwardAnim(model, toRad * backward, right, left);
            }
            if (sprint > 0.001F) {
                this.setupSprintingAnim(model, partialTicks, sprint, toRad * sprint, right, left);
            }
        }

        private float ease(float value) {
            value = Mth.clamp(value, 0.0F, 1.0F);
            return value * value * (3.0F - 2.0F * value);
        }

        public void setupBaseAnim(HumanoidModel<?> model, float toRad, boolean right, boolean left) {
            if (right) {
                model.rightArm.yRot += 45F * toRad;
                model.rightArm.zRot += 10F * toRad;
            }

            if (left) {
                model.leftArm.yRot -= 45F * toRad;
                model.leftArm.zRot -= 10F * toRad;
            }
            model.rightLeg.yRot += 20F * toRad;
            model.rightLeg.zRot += 5F * toRad;

            model.leftLeg.yRot -= 20F * toRad;
            model.leftLeg.zRot -= 5F * toRad;
        }

        public void setupForwardAnim(HumanoidModel<?> model, float forwardVal, float sprintVal, float toRad, boolean right, boolean left) {
            model.head.xRot -= 8F * toRad;

            if (right) {
                model.rightArm.xRot += 12.25F * toRad;
                model.rightArm.yRot -= 25F * toRad;
                model.rightArm.zRot += 15F * toRad;
            }
            if (left) {
                model.leftArm.xRot += 12.25F * toRad;
                model.leftArm.yRot += 25F * toRad;
                model.leftArm.zRot -= 15F * toRad;
            }

            model.rightLeg.xRot -= 5F * toRad * sprintVal;
            model.rightLeg.yRot -= model.rightLeg.yRot * forwardVal;
            model.rightLeg.zRot -= model.rightLeg.zRot * forwardVal;

            model.rightLeg.y -= forwardVal * sprintVal;
            model.rightLeg.z -= 1.5F * forwardVal * sprintVal;

            model.leftLeg.xRot += 7.5F * toRad;
            model.leftLeg.yRot -= 27.5F * toRad;
            model.leftLeg.zRot -= model.leftLeg.zRot * forwardVal;
        }

        public void setupBackwardAnim(HumanoidModel<?> model, float toRad, boolean right, boolean left) {
            model.head.xRot += 8F * toRad;

            if (right) {
                model.rightArm.xRot -= 15F * toRad;
                model.rightArm.yRot += 22.5F * toRad;
                model.rightArm.zRot += 15F * toRad;
            }
            if (left) {
                model.leftArm.xRot -= 15F * toRad;
                model.leftArm.yRot -= 22.5F * toRad;
                model.leftArm.zRot -= 15F * toRad;
            }

            model.rightLeg.xRot -= 15F * toRad;
            model.rightLeg.yRot += 20F * toRad;
            model.rightLeg.zRot += 7.5F * toRad;

            model.leftLeg.xRot -= 15F * toRad;
            model.leftLeg.yRot -= 20F * toRad;
            model.leftLeg.zRot -= 7.5F * toRad;
        }

        public void setupSprintingAnim(HumanoidModel<?> model, float partialTicks, float sprintVal, float toRad, boolean right, boolean left) {
            model.head.xRot -= model.head.xRot * sprintVal + sprintVal;
            if (this.ability.dataManager.get(ARM_AHEAD)) {
                if (right) {
                    model.rightArm.xRot -= 195 * toRad;
                    model.rightArm.yRot += 20F * toRad;
                    model.rightArm.zRot -= 30F * toRad;
                }
                if (left) {
                    model.leftArm.xRot -= 12.25F * toRad;
                    model.leftArm.zRot += 15F * toRad;
                }
            }
            for (HeatVisionAbility heatVisionAbility : CommonUtil.getAbilitiesByType(HeatVisionAbility.class, this.ability().getEntity())) {
                float f = heatVisionAbility.glowTimer.value(partialTicks);
                model.head.xRot -= sprintVal * f * 0.5F;
                if (right) {
                    model.rightArm.xRot -= model.rightArm.xRot * f;
                    model.rightArm.yRot -= model.rightArm.yRot * f;
                    model.rightArm.zRot -= model.rightArm.zRot * f;
                }
                if (left) {
                    model.leftArm.xRot -= model.leftArm.xRot * f;
                    model.leftArm.yRot -= model.leftArm.yRot * f;
                    model.leftArm.zRot -= model.leftArm.zRot * f;
                }
            }
            model.leftLeg.xRot -= model.leftLeg.xRot * sprintVal;
            model.leftLeg.yRot -= model.leftLeg.yRot * sprintVal;
            model.leftLeg.zRot -= model.leftLeg.zRot * sprintVal;

            model.rightLeg.xRot -= model.rightLeg.xRot * sprintVal;
            model.rightLeg.yRot -= model.rightLeg.yRot * sprintVal;
            model.rightLeg.zRot -= model.rightLeg.zRot * sprintVal;
        }
    }
}