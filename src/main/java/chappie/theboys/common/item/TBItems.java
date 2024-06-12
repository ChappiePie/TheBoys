package chappie.theboys.common.item;

import chappie.theboys.TheBoys;
import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.common.item.suit.SuitProperties;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Vector3f;

import java.util.function.Consumer;
import java.util.function.Function;

import static net.minecraft.world.item.ArmorItem.Type.*;

public class TBItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TheBoys.MODID);

    public static final ImmutableMap<ArmorItem.Type, RegistryObject<SuitItem>> HOMELANDER_SUIT = registerSuitParts(SuitItem::new, "homelander", (p) -> {}, CHESTPLATE, LEGGINGS, BOOTS);
    public static final ImmutableMap<ArmorItem.Type, RegistryObject<SuitItem>> ATRAIN_SUIT = registerSuitParts(SuitItem::new, "atrain", (p) -> p.armorScale((e, stack) ->
            p.getSlot() != EquipmentSlot.HEAD ? SuitProperties.BASIC_ARMOR_SCALE.apply(p.getSlot()) : new Vector3f(-Integer.MAX_VALUE)), HELMET, CHESTPLATE, LEGGINGS, BOOTS);
    public static final ImmutableMap<ArmorItem.Type, RegistryObject<SuitItem>> STARLIGHT_SUIT = registerSuitParts(SuitItem::new, "starlight", (p) -> p.armorScale((e, stack) -> new Vector3f(-Integer.MAX_VALUE)), CHESTPLATE, BOOTS);
    public static final ImmutableMap<ArmorItem.Type, RegistryObject<SuitItem>> TRANSLUCENT_SUIT = registerSuitParts(SuitItem::new, "translucent", (p) -> p.armorScale((e, stack) -> new Vector3f(-Integer.MAX_VALUE)), CHESTPLATE, LEGGINGS, BOOTS);

    public static final RegistryObject<SyringeItem> SYRINGE = ITEMS.register("syringe", SyringeItem::new);
    public static final RegistryObject<VialItem> VIAL = ITEMS.register("vial", VialItem::new);

    private static ImmutableMap<ArmorItem.Type, RegistryObject<SuitItem>> registerSuitParts(SuitSupplier supplier, String type, Consumer<SuitProperties> consumer, ArmorItem.Type... slots) {
        return TBItems.registerSuitParts(supplier, type, consumer, ArmorMaterials.LEATHER::getDefenseForType, 0.0F, slots);
    }

    private static ImmutableMap<ArmorItem.Type, RegistryObject<SuitItem>> registerSuitParts(SuitSupplier supplier, String type, Consumer<SuitProperties> consumer, Function<ArmorItem.Type, Integer> defense, double toughness, ArmorItem.Type... slots) {
        return TBItems.registerSuitParts(ITEMS, supplier, (p) -> {
            p.defense(defense.apply(p.slot)).toughness(toughness).stacksTo(1);
            consumer.accept(p);
        }, type, slots);
    }

    public static ImmutableMap<ArmorItem.Type, RegistryObject<SuitItem>> registerSuitParts(DeferredRegister<Item> registry, SuitSupplier supplier, Consumer<SuitProperties> consumer, String type, ArmorItem.Type... slots) {
        ImmutableMap.Builder<ArmorItem.Type, RegistryObject<SuitItem>> builder = ImmutableMap.builder();
        for (ArmorItem.Type slot : slots) {
            SuitProperties properties = new SuitProperties(type, slot);
            consumer.accept(properties);
            RegistryObject<SuitItem> registryObject = registry.register("%s_%s".formatted(type, slot.getName()), () ->
                    supplier.create(properties));
            builder.put(slot, registryObject);
        }
        return builder.build();
    }

    @FunctionalInterface
    private interface SuitSupplier {
        SuitItem create(SuitProperties pProperties);
    }
}