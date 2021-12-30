package chappie.theboys.common.items;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.heroesunited.heroesunited.hupacks.HUPackSuperpowers;

public class VialItem extends Item {

    public VialItem(Properties propertiesIn) {
        super(propertiesIn);
    }

    @Override
    public void fillItemCategory(CreativeModeTab itemGroup, NonNullList<ItemStack> items) {
        if (this.allowdedIn(itemGroup)) {
            items.add(new ItemStack(this));
            items.add(setInjection(new ItemStack(this), "compound_v"));

            for (ResourceLocation superpower : HUPackSuperpowers.getSuperpowersJSONS().keySet()) {
                items.add(setInjection(new ItemStack(this), superpower.toString()));
            }
        }

    }

    public static int getColor(ItemStack stack, int color) {
        int id = getInjection(stack).hashCode();
        if (color > 0 || StringUtil.isNullOrEmpty(getInjection(stack))) {
            return 16777215;
        }

        if (getInjection(stack).equals("compound_v")) {
            return 6009838;
        }
        return id;
    }

    public static String getInjection(ItemStack stack) {
        return stack.getOrCreateTag().getString("Injection");
    }

    public static ItemStack setInjection(ItemStack stack, String injection) {
        stack.getOrCreateTag().putString("Injection", injection);
        return stack;
    }
}