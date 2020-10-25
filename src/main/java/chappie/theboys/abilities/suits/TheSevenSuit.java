package chappie.theboys.abilities.suits;

import net.minecraft.item.ItemStack;
import xyz.heroesunited.generatorrex.abilities.suit.Suit;
import xyz.heroesunited.generatorrex.abilities.suit.SuitType;

public class TheSevenSuit extends Suit {
    public TheSevenSuit(SuitType type) {
        super(type);
    }

    public String getDescription(ItemStack stack) {
        return "The Seven: Let justice be done though the heavens fall";
    }
}
