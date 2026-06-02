package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.theboys.util.TBCommonUtil;
import net.minecraft.world.entity.LivingEntity;

public class DolphinCompanionAbility extends Ability {

    public DolphinCompanionAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(TBCommonUtil.DISTANCE, this.entity.level().random.nextIntBetweenInclusive(100, 200));
    }
}
