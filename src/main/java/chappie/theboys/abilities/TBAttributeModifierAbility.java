package chappie.theboys.abilities;

import chappie.theboys.common.capability.BoysCap;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import xyz.heroesunited.heroesunited.common.abilities.AttributeModifierAbility;

public class TBAttributeModifierAbility extends AttributeModifierAbility {

    public TBAttributeModifierAbility() {
       super(TBAbilityTypes.OVERLAY);
    }

    @Override
    public double getAmount(PlayerEntity player, JsonObject attribute) {
        double amount = super.getAmount(player, attribute);
        return BoysCap.getCap(player).haveCompoundV() ? amount * 1.5F : amount;
    }
}
