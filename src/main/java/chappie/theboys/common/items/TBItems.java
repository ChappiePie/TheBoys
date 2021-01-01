package chappie.theboys.common.items;

import chappie.theboys.TheBoys;
import net.minecraft.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TBItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TheBoys.MODID);

    public static final VialItem VIAL = register("vial", new VialItem(new Item.Properties().maxStackSize(1)));

    private static <T extends Item> T register(String name, T item) {
        ITEMS.register(name, () -> item);
        return item;
    }
}
