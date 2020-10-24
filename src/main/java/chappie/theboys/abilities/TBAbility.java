package chappie.theboys.abilities;

import chappie.theboys.common.capability.BoysCap;
import net.minecraft.entity.player.PlayerEntity;
import xyz.heroesunited.generatorrex.abilities.Ability;
import xyz.heroesunited.generatorrex.abilities.AbilityHelper;

public abstract class TBAbility extends Ability {

    public void onDeactivated(PlayerEntity player) {
        super.onDeactivated(player);
        if (AbilityHelper.getAbilities(player).size() >= 0 && AbilityHelper.getAbilities(player).size() != 1) {
            player.getCapability(BoysCap.CAPABILITY).ifPresent((a) -> {
                a.setSpeedLevel(0);
            });
        }

    }
}
