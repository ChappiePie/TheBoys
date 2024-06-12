package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.util.IHasTimer;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class SuperHearingAbility extends Ability implements IHasTimer {

    public Timer timer = new Timer(() -> 10, this::isEnabled);

    public SuperHearingAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public List<Timer> timers() {
        return List.of(this.timer);
    }
}
