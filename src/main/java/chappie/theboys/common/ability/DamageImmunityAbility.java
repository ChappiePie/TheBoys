package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.theboys.common.ability.base.TBAbilityTypes;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class DamageImmunityAbility extends Ability {
    public final List<String> damageSources = new ArrayList<>();

    public DamageImmunityAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    public static AbilityBuilder of(String id, String... damageSources) {
        return AbilityBuilder.of(id, TBAbilityTypes.DAMAGE_IMMUNITY).hide().additionalData(a -> {
            if (a instanceof DamageImmunityAbility ability) {
                ability.damageSources.addAll(List.of(damageSources));
            }
        });
    }
}