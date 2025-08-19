package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.data.DataAccessor;
import chappie.theboys.util.interfaces.ILivingEntityEx;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class FocusOnGoalAbility extends Ability {

    public static final DataAccessor<Integer> TARGET_ID = new DataAccessor<>("target_id", DataAccessor.DataSerializer.INT);
    public static final DataAccessor<Integer> FORWARD_IMPULSE = new DataAccessor<>("forward_impulse", DataAccessor.DataSerializer.INT);

    public FocusOnGoalAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(TARGET_ID, this.entity.getId(), false);
        this.dataManager.define(FORWARD_IMPULSE, 0, false);
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        if (entity.getCommandSenderWorld().isClientSide) {
            float f = entity.zza;
            if (this.dataManager.get(FORWARD_IMPULSE) != f) {
                this.dataManager.setFromClient(FORWARD_IMPULSE, Math.round(f));
            }
        }

        int targetId = this.dataManager.get(TARGET_ID);
        boolean bool = entity.getId() == targetId;
        if (enabled) {
            Entity target = entity.getCommandSenderWorld().getEntity(targetId);
            if (bool || target == null || !target.isAlive() || target.getEyePosition().distanceTo(entity.getEyePosition()) < 4 && !this.hasSpeedAbility()) {
                this.dataManager.set(TARGET_ID, entity.getId());
            }

            if (!bool && entity instanceof ILivingEntityEx ex) {
                double xOld = ex.theBoys$oldPos().x;
                double zOld = ex.theBoys$oldPos().z;
                if (target != null && target.isAlive()) {
                    Vec3 vec = target.getEyePosition();
                    Vec3 vec1 = entity.getEyePosition();
                    if (this.hasSpeedAbility()) {
                        if (this.dataManager.get(FORWARD_IMPULSE) > 0 && (Math.abs(entity.getX() - xOld) != 0 || Math.abs(entity.getZ() - zOld) != 0)) {
                            // сделай проверку смотрит ли на моба,если нет то пусть уезжает
                            if (vec.distanceTo(vec1) > 2) {
                                entity.setDeltaMovement(entity.getDeltaMovement().add(vec.subtract(vec1).multiply(0.01F, 0, 0.01F)));
                            } else {
                                for (LivingEntity e : entity.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class,
                                        CommonUtil.boxWithRange(entity.position(), 0.5D))) {
                                    if (e != entity) {
                                        e.hurt(e.damageSources().inWall(), 2);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (!bool) {
                this.dataManager.set(TARGET_ID, entity.getId());
            }
        }
    }

    public boolean hasSpeedAbility() {
        for (SpeedAbility speedAbility : CommonUtil.listOfType(SpeedAbility.class, CommonUtil.getAbilities(entity))) {
            if (speedAbility.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public float getYRot(float yRot, float partialTicks) {
        if (!this.dataManager.get(TARGET_ID).equals(this.entity.getId())) {
            Entity target = this.entity.getCommandSenderWorld().getEntity(this.dataManager.get(TARGET_ID));
            if (target != null && target.isAlive()) {
                Vec3 vec = target.getEyePosition(partialTicks);
                Vec3 vec1 = this.entity.getEyePosition(partialTicks);
                Vec3 vec3 = vec.subtract(vec1);

                if ((!this.hasSpeedAbility() || vec.distanceTo(vec1) > 2) && vec.distanceTo(vec1) < 40) {
                    return (float) -Mth.atan2(vec3.x(), vec3.z()) * Mth.RAD_TO_DEG;
                }
            }
        }
        return yRot;
    }

    public float getXRot(float xRot, float partialTicks) {
        if (!this.dataManager.get(TARGET_ID).equals(this.entity.getId())) {
            Entity target = this.entity.getCommandSenderWorld().getEntity(this.dataManager.get(TARGET_ID));
            if (target != null && target.isAlive()) {
                Vec3 vec = target.getEyePosition(partialTicks);
                Vec3 vec1 = this.entity.getEyePosition(partialTicks);
                Vec3 vec3 = vec.subtract(vec1);

                if ((!this.hasSpeedAbility() || vec.distanceTo(vec1) > 2) && vec.distanceTo(vec1) < 40) {
                    double e = vec3.x();
                    double g = vec3.z();
                    return (float) -Mth.atan2(vec3.y(), Math.sqrt(e * e + g * g)) * Mth.RAD_TO_DEG;
                }
            }
        }
        return xRot;
    }

    public boolean condition(Condition c, boolean key) {
        boolean b = key;
        for (Condition enabling : this.conditionManager.conditionsFor("enabling")) {
            if (enabling != c && !enabling.get()) {
                b = false;
            }
        }
        if (b) {
            if (this.dataManager.get(FocusOnGoalAbility.TARGET_ID).equals(this.entity.getId())) {
                var hitResult = CommonUtil.pick(this.entity, 40);
                if (hitResult instanceof EntityHitResult hr && hr.getEntity() instanceof LivingEntity target) {
                    if (target.getEyePosition().distanceTo(this.entity.getEyePosition()) < 4 && !this.hasSpeedAbility()) {
                        return false;
                    }
                    this.dataManager.set(FocusOnGoalAbility.TARGET_ID, target.getId());
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }
}