package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.data.DataAccessor;
import chappie.theboys.util.interfaces.ILivingEntityEx;
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
        this.dataManager.clientWritable(FORWARD_IMPULSE);
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        if (entity.level().isClientSide()) {
            float f = entity.zza;
            if (this.dataManager.get(FORWARD_IMPULSE) != f) {
                this.dataManager.setFromClient(FORWARD_IMPULSE, Math.round(f));
            }
        }

        int targetId = this.dataManager.get(TARGET_ID);
        boolean bool = entity.getId() == targetId;
        if (enabled) {
            Entity target = entity.level().getEntity(targetId);
            if (bool || target == null || !target.isAlive()
                    || target.getEyePosition().distanceTo(entity.getEyePosition()) < 4 && !this.hasSpeedAbility()) {
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
                                entity.setDeltaMovement(serverVelocity(entity).add(vec.subtract(vec1).multiply(0.01F, 0, 0.01F)));
                            } else {
                                for (LivingEntity e : entity.level().getEntitiesOfClass(LivingEntity.class,
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
        for (SpeedAbility speedAbility : CommonUtil.getAbilitiesByType(SpeedAbility.class, entity)) {
            if (speedAbility.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    private Vec3 serverVelocity(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return entity.getDeltaMovement();
        }
        if (entity instanceof ILivingEntityEx ex) {
            return entity.position().subtract(ex.theBoys$oldPos());
        }
        return Vec3.ZERO;
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
                    if (target.getEyePosition().distanceTo(this.entity.getEyePosition()) < 2 && !this.hasSpeedAbility()) {
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