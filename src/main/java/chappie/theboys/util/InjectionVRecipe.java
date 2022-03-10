package chappie.theboys.util;

import chappie.theboys.common.items.InjectionItem;
import chappie.theboys.common.items.TBItems;
import chappie.theboys.common.items.VialItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class InjectionVRecipe extends CustomRecipe {
    public InjectionVRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level world) {
        int i = 0;
        int j = 0;

        for(int k = 0; k < inv.getContainerSize(); ++k) {
            ItemStack itemstack = inv.getItem(k);
            if (!itemstack.isEmpty()) {
                if (itemstack.getItem() instanceof InjectionItem) {
                    ++i;
                } else {
                    if (itemstack.getItem() != TBItems.VIAL.get() || StringUtil.isNullOrEmpty(itemstack.getOrCreateTag().getString("Injection"))) {
                        return false;
                    }

                    ++j;
                }
                if (j > 1 || i > 1) {
                    return false;
                }
            }
        }
        return i == 1 && j == 1;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        for(int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof VialItem) {
                return InjectionItem.setInjection(TBItems.INJECTION.get().getDefaultInstance(), stack.getOrCreateTag().getString("Injection"));
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int p_194133_1_, int p_194133_2_) {
        return p_194133_1_ * p_194133_2_ >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TBRecipeSerializer.INJECTION_V;
    }
}