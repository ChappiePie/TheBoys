package chappie.theboys.common.items;

import chappie.theboys.TheBoys;
import net.minecraft.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TBItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TheBoys.MODID);

    /*public static final SuitItem ATRAIN_HELMET = register("atrain_helmet", new SuitItem(TBSuitTypes.ATRAIN, EquipmentSlotType.HEAD));
    public static final SuitItem ATRAIN_CHEST = register("atrain_chest", new SuitItem(TBSuitTypes.ATRAIN, EquipmentSlotType.CHEST));
    public static final SuitItem ATRAIN_LEGS = register("atrain_legs", new SuitItem(TBSuitTypes.ATRAIN, EquipmentSlotType.LEGS));
    public static final SuitItem ATRAIN_BOOTS = register("atrain_boots", new SuitItem(TBSuitTypes.ATRAIN, EquipmentSlotType.FEET));

    public static final SuitItem HOMELANDER_CHEST = register("homelander_chest", new SuitItem(TBSuitTypes.HOMELANDER, EquipmentSlotType.CHEST));
    public static final SuitItem HOMELANDER_LEGS = register("homelander_legs", new SuitItem(TBSuitTypes.HOMELANDER, EquipmentSlotType.LEGS));
    public static final SuitItem HOMELANDER_BOOTS = register("homelander_boots", new SuitItem(TBSuitTypes.HOMELANDER, EquipmentSlotType.FEET));*/

    private static <T extends Item> T register(String name, T item) {
        ITEMS.register(name, () -> item);
        return item;
    }
}
