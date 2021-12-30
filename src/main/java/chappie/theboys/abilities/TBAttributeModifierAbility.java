package chappie.theboys.abilities;

import chappie.theboys.common.capability.BoysCap;
import com.google.gson.JsonObject;
import net.minecraft.world.entity.player.Player;
import xyz.heroesunited.heroesunited.common.abilities.AttributeModifierAbility;

public class TBAttributeModifierAbility extends AttributeModifierAbility {

    public TBAttributeModifierAbility() {
       super(TBAbilityTypes.TB_ATTRIBUTE);
    }

    @Override
    public double getAmount(Player player, JsonObject attribute) {
        double amount = super.getAmount(player, attribute);
        return BoysCap.getCap(player).haveCompoundV() ? amount * 1.5F : amount;
    }
}
