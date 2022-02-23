package chappie.theboys.abilities;

import chappie.theboys.common.capability.BoysCap;
import com.google.gson.JsonObject;
import net.minecraft.world.entity.player.Player;
import xyz.heroesunited.heroesunited.common.abilities.AbilityType;
import xyz.heroesunited.heroesunited.common.abilities.AttributeModifierAbility;

public class TBAttributeModifierAbility extends AttributeModifierAbility {

    public TBAttributeModifierAbility(AbilityType type, Player player, JsonObject jsonObject) {
        super(type, player, jsonObject);
    }

    /** For some reason, sometimes HP is not updated, this fixes */
    @Override
    public void action(Player player) {
        super.action(player);
        player.setHealth(player.getHealth());
    }


    /** Multiply amount, if player uses compound V */
    @Override
    public double getAmount(JsonObject attribute) {
        double amount = super.getAmount(attribute);
        var cap = BoysCap.getCap(this.player);
        if (cap != null && cap.haveCompoundV()) {
            return amount * 1.5F;
        }
        return amount;
    }
}
