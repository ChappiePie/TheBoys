package chappie.theboys.common.items;

import chappie.theboys.TheBoys;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TBItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TheBoys.MODID);

    public static final RegistryObject<VialItem> VIAL = ITEMS.register("vial", () -> new VialItem(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS).stacksTo(1)));
    public static final RegistryObject<InjectionItem> INJECTION = ITEMS.register("injection", () -> new InjectionItem(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS).stacksTo(1)));
    public static final RegistryObject<ScrapItem> SCRAP = ITEMS.register("scrap", () -> new ScrapItem(3.0F, -3.1F, new Item.Properties()));
}
