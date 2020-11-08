package chappie.theboys.abilities.suits;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import xyz.heroesunited.heroesunited.common.abilities.suit.*;

import java.util.List;

public class TheSevenSuit extends Suit {
    public TheSevenSuit(SuitType type) {
        super(type);
    }

    public List<ITextComponent> getDescription(ItemStack stack) {
        List<ITextComponent> list = Lists.newArrayList();
        list.add(new TranslationTextComponent("theseven.motto").mergeStyle(TextFormatting.DARK_GRAY));
        return list;
    }
}
