package chappie.theboys.util;

import chappie.theboys.common.items.InjectionItem;
import chappie.theboys.common.items.TBItems;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class InjectionVRecipe extends SpecialRecipe {
    public InjectionVRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
        int i = 0;
        int j = 0;

        for(int k = 0; k < inv.getContainerSize(); ++k) {
            ItemStack itemstack = inv.getItem(k);
            if (!itemstack.isEmpty()) {
                if (itemstack.getItem() instanceof InjectionItem) {
                    ++i;
                } else {
                    if (itemstack.getItem() != TBItems.COMPOUND_V) {
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
    public ItemStack assemble(CraftingInventory inv) {
        return InjectionItem.setCompoundV(TBItems.INJECTION.getDefaultInstance(), true);
    }

    @Override
    public boolean canCraftInDimensions(int p_194133_1_, int p_194133_2_) {
        return p_194133_1_ * p_194133_2_ >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return TBRecipeSerializer.INJECTION_V;
    }
}