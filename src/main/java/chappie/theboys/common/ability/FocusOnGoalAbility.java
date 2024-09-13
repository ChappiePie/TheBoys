package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.util.data.DataAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

public class FocusOnGoalAbility extends Ability {

    public static final DataAccessor<String> TARGET_UUID = new DataAccessor<>("target", DataAccessor.DataSerializer.STRING);

    public FocusOnGoalAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(TARGET_UUID, "");
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        if (enabled) {
            var hitResult = entity.pick(100, 1, false);
            if (hitResult instanceof EntityHitResult hr) {
                if (hr.getEntity() instanceof LivingEntity target) {
                    this.dataManager.set(TARGET_UUID, target.getStringUUID());
                }
            }
        }
    }
}