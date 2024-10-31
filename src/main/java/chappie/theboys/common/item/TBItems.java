package chappie.theboys.common.item;

import chappie.modulus.common.ability.base.Superpower;
import chappie.theboys.TheBoys;
import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.common.item.suit.SuitProperties;
import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.minecraft.world.item.ArmorItem.Type.*;

public class TBItems {

    public static final ArrayList<Item> ITEMS = new ArrayList<>();
    public static final ArrayList<ItemStack> ITEMS_TAB = new ArrayList<>();

    public static final ImmutableMap<ArmorItem.Type, SuitItem> HOMELANDER_SUIT = registerSuitParts(SuitItem::new, "homelander", (p) -> {
    }, CHESTPLATE, LEGGINGS, BOOTS);
    public static final ImmutableMap<ArmorItem.Type, SuitItem> ATRAIN_SUIT = registerSuitParts(SuitItem::new, "atrain", (p) -> p.armorScale((e, stack) ->
            p.getSlot() != EquipmentSlot.HEAD ? SuitProperties.BASIC_ARMOR_SCALE.apply(p.getSlot()) : new Vector3f(-Integer.MAX_VALUE)), HELMET, CHESTPLATE, LEGGINGS, BOOTS);
    public static final ImmutableMap<ArmorItem.Type, SuitItem> STARLIGHT_SUIT = registerSuitParts(SuitItem::new, "starlight", (p) -> p.armorScale((e, stack) -> new Vector3f(-Integer.MAX_VALUE)), CHESTPLATE, BOOTS);
    public static final ImmutableMap<ArmorItem.Type, SuitItem> TRANSLUCENT_SUIT = registerSuitParts(SuitItem::new, "translucent", (p) -> p.armorScale((e, stack) -> new Vector3f(-Integer.MAX_VALUE)), CHESTPLATE, LEGGINGS, BOOTS);

    public static final SyringeItem SYRINGE = register("syringe", new SyringeItem(), false);
    public static final VialItem VIAL = register("vial", new VialItem(), false);

    public static final CreativeModeTab THE_BOYS_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TheBoys.id("theboys"), FabricItemGroup.builder()
            .icon(() -> new ItemStack(Objects.requireNonNull(TBItems.HOMELANDER_SUIT.get(CHESTPLATE))))
            .title(Component.translatable("title.theboys")).displayItems((itemDisplayParameters, output) -> {
                output.acceptAll(TBItems.ITEMS_TAB);

                output.accept(SYRINGE);
                output.accept(VIAL);
                output.accept(VialItem.compoundV());
                for (ResourceLocation resourceLocation : Superpower.REGISTRY.keySet()) {
                    if (resourceLocation.getNamespace().equals(TheBoys.MODID)) {
                        ItemStack pStack = new ItemStack(VIAL);
                        pStack.getOrCreateTag().putString("superpower", resourceLocation.toString());
                        output.accept(pStack);
                    }
                }
            }).build());

    private static ImmutableMap<ArmorItem.Type, SuitItem> registerSuitParts(SuitSupplier supplier, String type, Consumer<SuitProperties> consumer, ArmorItem.Type... slots) {
        return TBItems.registerSuitParts(supplier, type, consumer, ArmorMaterials.LEATHER::getDefenseForType, 0.0F, slots);
    }

    private static ImmutableMap<ArmorItem.Type, SuitItem> registerSuitParts(SuitSupplier supplier, String type, Consumer<SuitProperties> consumer, Function<ArmorItem.Type, Integer> defense, double toughness, ArmorItem.Type... slots) {
        ImmutableMap.Builder<ArmorItem.Type, SuitItem> builder = ImmutableMap.builder();
        for (ArmorItem.Type slot : slots) {
            SuitProperties properties = new SuitProperties(type, slot);
            consumer.accept(properties);
            properties.defense(defense.apply(properties.slot)).toughness(toughness).stacksTo(1);
            SuitItem item = register("%s_%s".formatted(type, slot.getName()), supplier.create(properties));
            builder.put(slot, item);
        }
        return builder.build();
    }

    public static <T extends Item> T register(String id, T item) {
        return register(id, item, true);
    }

    public static <T extends Item> T register(String id, T item, boolean ownSort) {
        T registered = Registry.register(BuiltInRegistries.ITEM, TheBoys.id(id), item);
        ITEMS.add(registered);
        if (ownSort) {
            ITEMS_TAB.add(new ItemStack(registered));
        }
        return registered;
    }

    public static void init() {
    }

    @FunctionalInterface
    private interface SuitSupplier {
        SuitItem create(SuitProperties pProperties);
    }
}