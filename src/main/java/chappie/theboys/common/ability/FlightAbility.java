package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.AbilityClientProperties;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.data.DataAccessor;
import chappie.modulus.util.events.SetupAnimCallback;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

public class FlightAbility extends Ability implements IHasTimer {

    public static final DataAccessor<Float> SPRINT_SPEED = new DataAccessor<>("sprint_speed", DataAccessor.DataSerializer.FLOAT);
    public static final DataAccessor<Float> SPEED = new DataAccessor<>("speed", DataAccessor.DataSerializer.FLOAT);
    public static final DataAccessor<Boolean> BREAK_BLOCKS = new DataAccessor<>("break_blocks", DataAccessor.DataSerializer.BOOLEAN);

    public static final DataAccessor<Boolean> BOOSTING = new DataAccessor<>("boosting", DataAccessor.DataSerializer.BOOLEAN);

    public static final DataAccessor<Boolean> SPRINTING = new DataAccessor<>("sprinting", DataAccessor.DataSerializer.BOOLEAN);
    public static final DataAccessor<Boolean> ARM_AHEAD = new DataAccessor<>("arm_ahead", DataAccessor.DataSerializer.BOOLEAN);
    public static final DataAccessor<Integer> FORWARD_IMPULSE = new DataAccessor<>("forward_impulse", DataAccessor.DataSerializer.INT);

    public final Timer timer = new Timer(() -> 5, this::isEnabled);
    public final Timer sprintingTimer = new Timer(() -> 10, () -> this.isEnabled() && this.dataManager.get(SPRINTING));
    public final Timer forwardTimer = new Timer(() -> 5, () -> this.isEnabled() && this.dataManager.get(FORWARD_IMPULSE) > 0);
    public final Timer backwardTimer = new Timer(() -> 5, () -> this.isEnabled() && this.dataManager.get(FORWARD_IMPULSE) < 0);

    public final Cooldown cooldown = new Cooldown();

    public FlightAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(SPEED, 1.0F);
        this.dataManager.define(SPRINT_SPEED, 2.0F);
        this.dataManager.define(BREAK_BLOCKS, true);
        this.dataManager.define(BOOSTING, false, false);

        this.dataManager.define(SPRINTING, false, false);
        this.dataManager.define(ARM_AHEAD, false, false);
        this.dataManager.define(FORWARD_IMPULSE, 0, false);
    }

    @Override
    public void onDataUpdated(DataAccessor<?> accessor) {
        super.onDataUpdated(accessor);
        if (accessor == SPRINTING) {
            if (this.dataManager.get(SPRINTING)) {
                this.dataManager.set(ARM_AHEAD, this.entity.getRandom().nextInt(100) < 20);
            } else {
                this.dataManager.set(ARM_AHEAD, false);
            }
        }
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        if (enabled) {
            if (entity.getCommandSenderWorld().isClientSide) {
                float f = entity.zza;
                boolean sprinting = entity.isSprinting();
                if (this.dataManager.get(SPRINTING) != sprinting) {
                    this.dataManager.setFromClient(SPRINTING, sprinting);
                }
                if (this.dataManager.get(FORWARD_IMPULSE) != f) {
                    this.dataManager.setFromClient(FORWARD_IMPULSE, Math.round(f));
                }
            }
            boolean sprinting = this.dataManager.get(SPRINTING) || entity.isSprinting();
            if (!sprinting || this.dataManager.get(FORWARD_IMPULSE) <= 0) {
                this.dataManager.set(BOOSTING, false);
            }
            if (!(entity instanceof Player) && this.enabledTicks == 0) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.4, 0));
            }

            float speed = sprinting ? this.dataManager.get(SPRINT_SPEED) : this.dataManager.get(SPEED);
            Vec3 vec3;
            if (sprinting) {
                if (this.cooldown.end()) {
                    if (this.conditionManager.test("boost")) {
                        this.cooldown.start(60);
                        this.dataManager.set(BOOSTING, true);
                        CommonUtil.spawnParticleForAll(this.entity.getCommandSenderWorld(), ParticleTypes.EXPLOSION,
                                true, this.entity.position(), Vec3.ZERO, 1, 10);
                    }
                }
                if (this.dataManager.get(BOOSTING)) {
                    speed += this.cooldown.value(1) * 4F;
                    CommonUtil.spawnParticleForAll(this.entity.getCommandSenderWorld(), ParticleTypes.CLOUD,
                            true, this.entity.position(), Vec3.ZERO, 0.05F, 10);
                    if (this.cooldown.timer == 0) {
                        this.dataManager.set(BOOSTING, false);
                    }
                }
                vec3 = entity.getDeltaMovement().scale(0.25F).add(entity.getLookAngle().scale(speed));
            } else {
                vec3 = entity.getDeltaMovement().multiply(1.05, 0.1F, 1.05); // slight sliding effect
                vec3 = vec3.add(0, Math.sin(entity.tickCount / 10F) / 50F, 0); // hover
                vec3 = vec3.add(inputVector(entity, speed * 2)); // unite two vectors, default and with movements.
            }
            entity.setDeltaMovement(vec3);
            entity.fallDistance = 0.0F;
        } else {
            this.dataManager.set(BOOSTING, false);
        }
    }

    private Vec3 inputVector(LivingEntity entity, float speedModifier) {
        double yya = entity.zza == 0 ? 0 : entity.getLookAngle().y; // stop flight by y, when don't use any keys
        yya = entity.zza < 0 ? -yya : yya; // flight by y when using up or down keys
        speedModifier *= 0.01F;
        Vec3 vec = new Vec3(entity.xxa, yya, entity.zza);
        if (entity.xxa == 0 && entity.zza == 0) {
            vec = entity.getDeltaMovement();
        }

        double d0 = vec.lengthSqr();
        if (d0 < 1.0E-7D) {
            return Vec3.ZERO;
        } else {
            Vec3 vec3 = (d0 > 1.0D ? vec.normalize() : vec).scale(speedModifier);
            double f = Math.sin(Math.toRadians(entity.getYRot()));
            double f1 = Math.cos(Math.toRadians(entity.getYRot()));
            return new Vec3(vec3.x * f1 - vec3.z * f, yya * speedModifier * 16, vec3.z * f1 + vec3.x * f);
        }
    }

    @Override
    public void initializeClient(Consumer<AbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new FlightClientProperties(this));
    }

    @Override
    public List<Timer> timers() {
        return List.of(this.timer, this.sprintingTimer, this.forwardTimer, this.backwardTimer, this.cooldown);
    }

    public record FlightClientProperties(FlightAbility ability) implements AbilityClientProperties {

        @Override
        public void setupAnim(SetupAnimCallback.SetupAnimEvent event) {
            AbilityClientProperties.super.setupAnim(event);
            LivingEntity entity = event.entity();
            var properties = event.modelProperties();
            HumanoidModel<?> model = event.model();
            float f = this.ability.timer.value(properties.partialTicks());
            float f1 = this.ability.forwardTimer.value(properties.partialTicks());
            float f2 = this.ability.backwardTimer.value(properties.partialTicks());
            float f3 = this.ability.sprintingTimer.value(properties.partialTicks()) * f;
            float f4 = 1.0F - (f1 + f2) / 2.0F;
            float toRad = (float) Math.toRadians(f);
            if (!(event.state() instanceof ArmedEntityRenderState state)) return;
            float bob = Mth.sin(state.ageInTicks * 0.067F) * 0.05F;


            boolean right = !entity.isUsingItem() && state.rightArmPose == HumanoidModel.ArmPose.EMPTY && !state.leftArmPose.isTwoHanded();
            boolean left = !entity.isUsingItem() && state.leftArmPose == HumanoidModel.ArmPose.EMPTY && !state.rightArmPose.isTwoHanded();

            model.head.xRot /= 1 + f;
            model.head.xRot += bob * f;
            model.body.y -= bob * 4F * f;
            float f5 = (1.0F - this.ability.sprintingTimer.value(properties.partialTicks())) * f;
            if (right) {
                model.rightArm.xRot += bob * 3.0F * f5;
            }
            if (left) {
                model.leftArm.xRot += bob * 3.0F * f5;
            }
            model.rightLeg.xRot -= model.rightLeg.xRot * f;
            model.rightLeg.xRot += bob * f;
            model.leftLeg.xRot -= model.leftLeg.xRot * f;
            model.leftLeg.xRot -= bob * f;

            if (f4 != 0) {
                this.setupBaseAnim(model, toRad * f4, right, left);
            }
            if (f1 != 0) {
                this.setupForwardAnim(model, f1, f4, toRad * f1, right, left);
            }
            if (f2 != 0) {
                this.setupBackwardAnim(model, toRad * f2, right, left);
            }
            if (f3 != 0) {
                this.setupSprintingAnim(model, properties.partialTicks(), f3, toRad * f3, right, left);
            }
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
            for (HeatVisionAbility heatVisionAbility : CommonUtil.listOfType(HeatVisionAbility.class, CommonUtil.getAbilities(this.ability().entity))) {
                float f = heatVisionAbility.eyesTimer.value(partialTicks);
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