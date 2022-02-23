package chappie.theboys.common.items;

import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.heroesunited.heroesunited.hupacks.HUPackSuperpowers;

import java.util.List;

public class VialItem extends Item {

    public VialItem(Properties propertiesIn) {
        super(propertiesIn);
    }

    @Override
    public void fillItemCategory(CreativeModeTab itemGroup, NonNullList<ItemStack> items) {
        if (this.allowdedIn(itemGroup)) {
            items.add(new ItemStack(this));
            items.add(InjectionItem.setInjection(new ItemStack(this), "compound_v"));

            for (ResourceLocation superpower : HUPackSuperpowers.getSuperpowers().keySet()) {
                items.add(InjectionItem.setInjection(new ItemStack(this), superpower.toString()));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        String superpower = InjectionItem.getInjection(pStack);
        if (!StringUtil.isNullOrEmpty(superpower)) {
            TranslatableComponent injection;
            if (superpower.equals("compound_v")) {
                injection = new TranslatableComponent("injection.theboys.compound_v");
            } else {
                injection = new TranslatableComponent(Util.makeDescriptionId("superpowers", ResourceLocation.tryParse(superpower)));
            }
            pTooltipComponents.add(new TranslatableComponent("injection.theboys.tooltip", injection));
        }
    }
}