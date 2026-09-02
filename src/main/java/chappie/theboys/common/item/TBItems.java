package chappie.theboys.common.item;

import chappie.modulus.common.ability.base.Superpower;
import chappie.theboys.TheBoys;
import chappie.theboys.common.item.datacomponents.TBDataComponents;
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
import net.minecraft.world.item.Item.Properties;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.minecraft.world.item.ArmorItem.Type.*;


public class TBItems {

    public static final ArrayList<Item> ITEMS = new ArrayList<>();
    public static final ArrayList<ItemStack> ITEMS_TAB = new ArrayList<>();

    public static final ImmutableMap<ArmorItem.Type, SuitItem> HOMELANDER_SUIT = registerSuitParts("homelander", (p) -> {}, CHESTPLATE, LEGGINGS, BOOTS);
    public static final ImmutableMap<ArmorItem.Type, SuitItem> ATRAIN_SUIT = registerSuitParts("atrain", (p) -> p.armorScale((stack) ->
            p.getSlot() != EquipmentSlot.HEAD ? SuitProperties.BASIC_ARMOR_SCALE.apply(p.getSlot()) : new Vector3f(-Integer.MAX_VALUE)), HELMET, CHESTPLATE, LEGGINGS, BOOTS);
    //public static final ImmutableMap<ArmorItem.Type, SuitItem> STARLIGHT_SUIT = registerSuitParts(SuitItem::new, "starlight", (p) -> p.armorScale((stack) -> new Vector3f(-Integer.MAX_VALUE)), CHESTPLATE, BOOTS);
    //public static final ImmutableMap<ArmorItem.Type, SuitItem> BLACK_NOIR_SUIT = registerSuitParts("black_noir", (p) -> {}, HELMET, CHESTPLATE, LEGGINGS, BOOTS);
    //public static final ImmutableMap<ArmorItem.Type, SuitItem> STORMFRONT_SUIT = registerSuitParts(SuitItem::new, "stormfront", (p) -> p.armorScale((stack) -> new Vector3f(-Integer.MAX_VALUE)), CHESTPLATE, LEGGINGS, BOOTS);
    public static final ImmutableMap<ArmorItem.Type, SuitItem> THE_DEEP_SUIT = registerSuitParts("the_deep", (p) -> p.armorScale((stack) -> new Vector3f(-Integer.MAX_VALUE)), CHESTPLATE, LEGGINGS, BOOTS);
    public static final ImmutableMap<ArmorItem.Type, SuitItem> TRANSLUCENT_SUIT = registerSuitParts("translucent", (p) -> {}, CHESTPLATE, LEGGINGS, BOOTS);

    public static final SyringeItem SYRINGE = register("syringe", SyringeItem::new, new Properties(), false);
    public static final VialItem VIAL = register("vial", VialItem::new, new Properties(), false);

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
                        pStack.set(TBDataComponents.SUPERPOWER, resourceLocation.toString());
                        output.accept(pStack);
                    }
                }
            }).build());

    private static ImmutableMap<ArmorItem.Type, SuitItem> registerSuitParts(String type, Consumer<SuitProperties> consumer, ArmorItem.Type... slots) {
        return TBItems.registerSuitParts(type, consumer, ArmorMaterials.LEATHER.value().defense(), 0.0F, slots);
    }

    private static ImmutableMap<ArmorItem.Type, SuitItem> registerSuitParts(String type, Consumer<SuitProperties> consumer, Map<ArmorItem.Type, Integer> defense, double toughness, ArmorItem.Type... slots) {
        ImmutableMap.Builder<ArmorItem.Type, SuitItem> builder = ImmutableMap.builder();
        for (ArmorItem.Type slot : slots) {
            SuitProperties properties = new SuitProperties(type, slot);
            consumer.accept(properties);
            properties.defense(defense.get(properties.slot)).toughness(toughness).stacksTo(1);
            builder.put(slot, register("%s_%s".formatted(type, type.equals("atrain") && slot == HELMET ? "glasses" : slot.getName()), (prop) -> new SuitItem((SuitProperties) prop), properties));
        }
        return builder.build();
    }

    public static <T extends Item> T register(String id, Function<Properties, T> item, Properties properties) {
        return register(id, item, properties, true);
    }

    public static <T extends Item> T register(String id, Function<Properties, T> item, Properties properties, boolean ownSort) {
        T registered = Registry.register(BuiltInRegistries.ITEM, TheBoys.id(id), item.apply(properties
        //        .setId(ResourceKey.create(Registries.ITEM, TheBoys.id(id)))
        ));
        ITEMS.add(registered);
        if (ownSort) {
            ITEMS_TAB.add(new ItemStack(registered));
        }
        return registered;
    }

    public static void init() {
    }
}