package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.AbilityClientProperties;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.events.SetupAnimCallback;
import chappie.theboys.common.particle.TBParticleTypes;
import chappie.theboys.util.TBCommonUtil;
import com.google.common.collect.Lists;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class FishSwarmAbility extends Ability implements IHasTimer {

    public static final int DETECTION_RADIUS = 200;

    private final IHasTimer.Timer timer = new IHasTimer.Timer(() -> 10, this::isEnabled);

    public FishSwarmAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(TBCommonUtil.DISTANCE, this.entity.level().random.nextIntBetweenInclusive(50, 100));
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        if (enabled && entity.isInWater()) {
            Level level = entity.level();
            BlockPos playerPos = entity.blockPosition();
            double waterSurfaceY = entity.getY();

            for (int y = 0; y <= 10; y++) {
                BlockPos checkPos = playerPos.above(y);
                if (level.getFluidState(checkPos).is(FluidTags.WATER) &&
                        !level.getFluidState(checkPos.above()).is(FluidTags.WATER)) {
                    waterSurfaceY = checkPos.getY() + 1.0;
                    break;
                }
            }

            double distanceToSurface = waterSurfaceY - Math.ceil(entity.getY() + (entity.isCrouching() ? -0.75F : 0));
            double verticalMotion = distanceToSurface > 2.0 ? 0.2 :
                    distanceToSurface > 0.5 ? 0.12 :
                            distanceToSurface > 0 ? 0.08 :
                                    distanceToSurface < -0.7 ? -0.05 : 0.02;

            entity.setDeltaMovement(Vec3.directionFromRotation(0, entity.getYRot()).multiply(0.75, 0, 0.75)
                    .add(0, verticalMotion, 0));
            CommonUtil.spawnParticleForAll(this.entity.level(),
                    TBParticleTypes.WATER_SPLASH,
                    true, entity.position().add(0, 0.4F, 0), new Vec3(1, 0.2, 1), 1F, 2);

            CommonUtil.spawnParticleForAll(this.entity.level(),
                    TBParticleTypes.WATER_SPLASH,
                    true, entity.position().add(0, 0.4F, 0), new Vec3(0, 0, 0), 1F, 5);
        }
    }

    @Override
    public void initializeClient(Consumer<AbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new AbilityClientProperties() {
            private final Map<ModelPart, Vector3f> savedPose = new HashMap<>();

            @Override
            public void setupAnim(SetupAnimCallback.SetupAnimEvent event) {
                AbilityClientProperties.super.setupAnim(event);
                float ageInTicks = event.entity().tickCount + event.modelProperties().partialTicks();
                LivingEntity entity = event.entity();
                var model = event.model();
                HumanoidArm mainArm = entity instanceof Player player ? player.getMainArm() : HumanoidArm.RIGHT;
                float f = timer.value(event.modelProperties().partialTicks());
                if (f <= 0.001f) return;

                Vec3 motion = entity.getDeltaMovement();
                float speed = (float) motion.length();
                float speedFactor = Math.min(speed * 3.0f, 1.0f);

                List<ModelPart> modelPartList = Lists.newArrayList(model.body, model.leftArm, model.rightArm, model.leftLeg, model.rightLeg);
                for (ModelPart modelPart : modelPartList) {
                    this.savedPose.put(modelPart, new Vector3f(modelPart.xRot, modelPart.yRot, modelPart.zRot));
                }

                this.applyFullSurfingAnimation(model, ageInTicks, speedFactor, entity.isCrouching(), entity, mainArm == HumanoidArm.LEFT);

                this.savedPose.forEach((part, vec3f) ->
                        part.setRotation(Mth.lerp(f, vec3f.x, part.xRot), Mth.lerp(f, vec3f.y, part.yRot), Mth.lerp(f, vec3f.z, part.zRot)));
            }

            private void applyFullSurfingAnimation(HumanoidModel<? extends LivingEntityRenderState> model, float ageInTicks,
                                                          float speedFactor, boolean isCrouching,
                                                          Entity entity, boolean isLeftHanded) {
                // Pose reset
                model.body.setRotation(0, 0, 0);
                model.leftArm.setRotation(0, 0, 0);
                model.rightArm.setRotation(0, 0, 0);
                model.leftLeg.setRotation(0, 0, 0);
                model.rightLeg.setRotation(0, 0, 0);

                // base surfing pose
                {
                    model.body.xRot -= (float) Math.toRadians(-2 - speedFactor * 8);

                    model.leftArm.xRot = (float) Math.toRadians(-10);
                    model.leftArm.yRot = (float) Math.toRadians(-15);
                    model.leftArm.zRot = (float) Math.toRadians(-12);

                    model.rightArm.xRot = (float) Math.toRadians(-35);
                    model.rightArm.yRot = (float) Math.toRadians(15);
                    model.rightArm.zRot = (float) Math.toRadians(12);

                    float legBend = 8 + speedFactor * 12;
                    model.leftLeg.xRot = (float) Math.toRadians(-legBend);
                    model.leftLeg.zRot = (float) Math.toRadians(-10);

                    model.rightLeg.xRot = (float) Math.toRadians(legBend);
                    model.rightLeg.yRot = (float) Math.toRadians(20);
                    model.rightLeg.zRot = (float) Math.toRadians(2);

                    if (isCrouching) {
                        model.body.xRot += (float) Math.toRadians(32);
                        model.leftLeg.xRot -= (float) Math.toRadians(18);
                        model.rightLeg.xRot += (float) Math.toRadians(18);
                        model.leftArm.xRot += (float) Math.toRadians(10);
                        model.rightArm.xRot += (float) Math.toRadians(10);
                    }
                }

                // breath
                {
                    float breathCycle = Mth.sin(ageInTicks * 0.04f) * 0.025f;
                    model.body.xRot += breathCycle * 0.5f;
                    model.leftArm.xRot += breathCycle * 0.3f;
                    model.rightArm.xRot += breathCycle * 0.3f;
                }

                //bobbing
                {
                    float mainWave = Mth.sin(ageInTicks * 0.12f) * 0.15f;
                    float secondaryWave = Mth.sin(ageInTicks * 0.12f * 2.3f) * 0.15f * 0.3f;
                    float totalWave = (mainWave + secondaryWave) * (0.5f + speedFactor / 2F);
                    totalWave = isCrouching ? totalWave * 0.7F : totalWave;

                    model.body.xRot += totalWave * 0.8f;
                    model.leftLeg.xRot -= totalWave * 1.2f;
                    model.rightLeg.xRot -= totalWave * 1.2f;
                    model.leftArm.xRot -= totalWave * 0.5f;
                    model.rightArm.xRot -= totalWave * 0.5f;
                }
                {
                    float intensity = 0.3f + speedFactor * 0.7f;
                    float x = Mth.sin(ageInTicks * 0.51f) * 0.03f * intensity;
                    float z = Mth.cos(ageInTicks * 0.63f) * 0.03f * intensity;

                    model.body.zRot += z;
                    model.leftArm.zRot += z * 0.8f;
                    model.rightArm.zRot -= z * 0.8f;
                    model.leftArm.xRot += x * 0.5f;
                    model.rightArm.xRot -= x * 0.5f;
                }
                applyArmBalance(model, ageInTicks, speedFactor);
                applyLegBalance(model, ageInTicks, speedFactor, isCrouching);

                if (speedFactor >= 0.1f) {
                    model.body.xRot -= speedFactor * 0.15f;
                    model.leftLeg.xRot += speedFactor * 0.2f;
                    model.rightLeg.xRot += speedFactor * 0.2f;

                    float speedSway = Mth.sin(entity.tickCount * 0.25f) * speedFactor * 0.15f;
                    model.leftArm.yRot += speedSway;
                    model.rightArm.yRot -= speedSway;
                    model.leftArm.zRot -= speedFactor * 0.08f;
                    model.rightArm.zRot += speedFactor * 0.08f;

                    Vec3 motion = entity.getDeltaMovement();
                    if (motion.lengthSqr() > 0.001) {
                        float lateralTilt = (float) motion.x * 0.3f;
                        model.body.zRot += Mth.clamp(lateralTilt, -0.15f, 0.15f);
                    }
                }

                // sharp turns when surfing
                if (Math.abs(entity.getYRot() - entity.yRotO) > 10) {
                    float d = (entity.getYRot() - entity.yRotO) * 0.01f;
                    model.leftArm.zRot += d * 0.3f;
                    model.rightArm.zRot -= d * 0.3f;
                    model.leftArm.xRot -= Math.abs(d) * 0.2f;
                    model.rightArm.xRot -= Math.abs(d) * 0.2f;
                    model.body.zRot -= d * 0.15f;
                }

                // mirror animation
                if (isLeftHanded) {
                    Vector3f leftArmRot = new Vector3f(model.leftArm.xRot, model.leftArm.yRot, model.leftArm.zRot);
                    Vector3f leftLegRot = new Vector3f(model.leftLeg.xRot, model.leftLeg.yRot, model.leftLeg.zRot);

                    model.leftArm.setRotation(model.rightArm.xRot, -model.rightArm.yRot, -model.rightArm.zRot);
                    model.leftLeg.setRotation(model.rightLeg.xRot, -model.rightLeg.yRot, -model.rightLeg.zRot);

                    model.rightArm.setRotation(leftArmRot.x, -leftArmRot.y, -leftArmRot.z);
                    model.rightLeg.setRotation(leftLegRot.x, -leftLegRot.y, -leftLegRot.z);

                    model.body.zRot = -model.body.zRot;
                }
            }

            private void applyArmBalance(HumanoidModel<? extends LivingEntityRenderState> model, float ageInTicks, float speedFactor) {
                // arm balancing speed
                float armSpeed = 0.08f;

                float intensity = 0.4f + speedFactor * 0.6f;
                float leftArmCycle = Mth.sin(ageInTicks * armSpeed) * 0.12f * intensity;
                float rightArmCycle = Mth.sin(ageInTicks * armSpeed + Mth.PI) * 0.12f * intensity;

                model.leftArm.xRot += leftArmCycle;
                model.rightArm.xRot += rightArmCycle;
                model.leftArm.zRot += leftArmCycle * 0.6f;
                model.rightArm.zRot += rightArmCycle * 0.6f;
                armSpeed *= 1.3F;
                intensity *= 0.05F;
                model.leftArm.yRot += Mth.cos(ageInTicks * armSpeed) * intensity;
                model.rightArm.yRot += Mth.cos(ageInTicks * armSpeed + Mth.PI) * intensity;
            }

            private void applyLegBalance(HumanoidModel<? extends LivingEntityRenderState> model, float ageInTicks, float speedFactor, boolean isCrouching) {
                float legSpeed = 0.1f;

                float leftLegAdjust = Mth.sin(ageInTicks * legSpeed) * 0.06f;
                float rightLegAdjust = Mth.sin(ageInTicks * legSpeed + 1.0f) * 0.06f;
                float intensity = (0.5f + speedFactor * 0.5f) * (isCrouching ? 0.6f : 1.0f);

                model.leftLeg.xRot += leftLegAdjust * intensity;
                model.rightLeg.xRot += rightLegAdjust * intensity;
                model.leftLeg.yRot += leftLegAdjust * 0.3f * intensity;
                model.rightLeg.yRot += rightLegAdjust * 0.3f * intensity;
                legSpeed *= 1.5F;
                model.leftLeg.zRot += Mth.cos(ageInTicks * legSpeed) * 0.02f * intensity;
                model.rightLeg.zRot += Mth.cos(ageInTicks * legSpeed + Mth.PI) * 0.02f * intensity;
            }

        });
    }


    @Override
    public Iterable<Timer> timers() {
        return List.of(this.timer);
    }
}
