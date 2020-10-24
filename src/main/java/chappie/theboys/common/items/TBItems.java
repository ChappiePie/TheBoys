package chappie.theboys.common.items;

import chappie.theboys.TheBoys;
import chappie.theboys.abilities.suits.TBSuitTypes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.heroesunited.generatorrex.abilities.suit.SuitItem;

public class TBItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TheBoys.MODID);

    public static final RegistryObject<SuitItem> ATRAIN_HELMET = ITEMS.register("atrain_helmet", () -> new SuitItem(TBSuitTypes.ATRAIN, EquipmentSlotType.HEAD));
    public static final RegistryObject<SuitItem> ATRAIN_CHEST = ITEMS.register("atrain_chest", () -> new SuitItem(TBSuitTypes.ATRAIN, EquipmentSlotType.CHEST));
    public static final RegistryObject<SuitItem> ATRAIN_LEGS = ITEMS.register("atrain_legs", () -> new SuitItem(TBSuitTypes.ATRAIN, EquipmentSlotType.LEGS));
    public static final RegistryObject<SuitItem> ATRAIN_BOOTS = ITEMS.register("atrain_boots", () -> new SuitItem(TBSuitTypes.ATRAIN, EquipmentSlotType.FEET));

    public static final RegistryObject<SuitItem> HOMELANDER_CHEST = ITEMS.register("homelander_chest", () -> new SuitItem(TBSuitTypes.HOMELANDER, EquipmentSlotType.CHEST));
    public static final RegistryObject<SuitItem> HOMELANDER_LEGS = ITEMS.register("homelander_legs", () -> new SuitItem(TBSuitTypes.HOMELANDER, EquipmentSlotType.LEGS));
    public static final RegistryObject<SuitItem> HOMELANDER_BOOTS = ITEMS.register("homelander_boots", () -> new SuitItem(TBSuitTypes.HOMELANDER, EquipmentSlotType.FEET));
}
