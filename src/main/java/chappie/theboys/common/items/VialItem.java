package chappie.theboys.common.items;

import com.google.common.collect.Lists;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.StringUtils;
import xyz.heroesunited.heroesunited.common.abilities.Superpower;
import xyz.heroesunited.heroesunited.hupacks.HUPackSuperpowers;

import java.util.Random;

public class VialItem extends Item {
    private Random rand = new Random();

    public VialItem(Properties propertiesIn) {
        super(propertiesIn);
    }

    @Override
    public void fillItemCategory(ItemGroup itemGroup, NonNullList<ItemStack> items) {
        if (this.allowdedIn(itemGroup)) {
            items.add(new ItemStack(this));
            items.add(setInjection(new ItemStack(this), "compound_v"));

            if (HUPackSuperpowers.getInstance() != null) {
                for (Superpower superpower : HUPackSuperpowers.getSuperpowers().values()) {
                    items.add(setInjection(new ItemStack(this), superpower.getRegistryName().toString()));
                }
            }
        }

    }

    public int getColor(ItemStack stack, int color) {
        final int id = rand.nextInt(DyeColor.values().length);
        if (color > 0 || StringUtils.isNullOrEmpty(getInjection(stack))) {
            return 16777215;
        }

        if (getInjection(stack).equals("compound_v")) {
            return 6009838;
        }
        return Lists.newArrayList(DyeColor.values()).get(id).getColorValue();
    }

    public static String getInjection(ItemStack stack) {
        return stack.getOrCreateTag().getString("Injection");
    }

    public static ItemStack setInjection(ItemStack stack, String injection) {
        stack.getOrCreateTag().putString("Injection", injection);
        return stack;
    }
}