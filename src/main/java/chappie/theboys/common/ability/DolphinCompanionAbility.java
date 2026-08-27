package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.util.data.CommonAccessors;
import net.minecraft.world.entity.LivingEntity;

public class DolphinCompanionAbility extends Ability {

    public DolphinCompanionAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(CommonAccessors.DISTANCE, this.entity.level().getRandom().nextIntBetweenInclusive(100, 200));
    }
}