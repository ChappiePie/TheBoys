package chappie.theboys.common.items;

import chappie.theboys.TheBoys;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TBItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TheBoys.MODID);

    public static final VialItem VIAL = register("vial", new VialItem(new Item.Properties().tab(ItemGroup.TAB_TOOLS).stacksTo(1)));
    public static final InjectionItem INJECTION = register("injection", new InjectionItem(new Item.Properties().tab(ItemGroup.TAB_TOOLS).stacksTo(1)));
    public static final ScrapItem SCRAP = register("scrap", new ScrapItem(3.0F, -3.1F, new Item.Properties()));

    private static <T extends Item> T register(String name, T item) {
        ITEMS.register(name, () -> item);
        return item;
    }
}
