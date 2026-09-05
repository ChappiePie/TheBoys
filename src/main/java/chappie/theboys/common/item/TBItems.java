package chappie.theboys.common.item;

import chappie.modulus.common.ability.base.Superpower;
import chappie.theboys.TheBoys;
import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.common.item.suit.SuitProperties;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraft.world.item.ArmorItem.Type.*;


public class TBItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, TheBoys.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheBoys.MODID);

    public static final ArrayList<Supplier<ItemStack>> ITEMS_TAB = new ArrayList<>();

    public static final ImmutableMap<ArmorItem.Type, DeferredHolder<Item, SuitItem>> HOMELANDER_SUIT = registerSuitParts("homelander", (p) -> {}, CHESTPLATE, LEGGINGS, BOOTS);
    public static final ImmutableMap<ArmorItem.Type, DeferredHolder<Item, SuitItem>> ATRAIN_SUIT = registerSuitParts("atrain", (p) -> p.armorScale((stack) ->
            p.getSlot() != EquipmentSlot.HEAD ? SuitProperties.BASIC_ARMOR_SCALE.apply(p.getSlot()) : new Vector3f(-Integer.MAX_VALUE)), HELMET, CHESTPLATE, LEGGINGS, BOOTS);
    public static final ImmutableMap<ArmorItem.Type, DeferredHolder<Item, SuitItem>> BLACK_NOIR_SUIT = registerSuitParts("black_noir", (p) -> {
    }, HELMET, CHESTPLATE, LEGGINGS, BOOTS);
    public static final ImmutableMap<ArmorItem.Type, DeferredHolder<Item, SuitItem>> THE_DEEP_SUIT = registerSuitParts("the_deep", (p) -> p.armorScale((stack) -> new Vector3f(-Integer.MAX_VALUE)), CHESTPLATE, LEGGINGS, BOOTS);
    public static final ImmutableMap<ArmorItem.Type, DeferredHolder<Item, SuitItem>> TRANSLUCENT_SUIT = registerSuitParts("translucent", (p) -> {}, CHESTPLATE, LEGGINGS, BOOTS);

    public static final DeferredHolder<Item, SyringeItem> SYRINGE = register("syringe", () -> new SyringeItem(new Properties()), false);
    public static final DeferredHolder<Item, VialItem> VIAL = register("vial", () -> new VialItem(new Properties()), false);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THE_BOYS_TAB = CREATIVE_TABS.register("theboys", () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(HOMELANDER_SUIT.get(CHESTPLATE).get()))
            .title(Component.translatable("title.theboys"))
            .build());

    private static ImmutableMap<ArmorItem.Type, DeferredHolder<Item, SuitItem>> registerSuitParts(String type, Consumer<SuitProperties> consumer, ArmorItem.Type... slots) {
        return TBItems.registerSuitParts(type, consumer, ArmorMaterials.LEATHER.value().defense(), 0.0F, slots);
    }

    private static ImmutableMap<ArmorItem.Type, DeferredHolder<Item, SuitItem>> registerSuitParts(String type, Consumer<SuitProperties> consumer, Map<ArmorItem.Type, Integer> defense, double toughness, ArmorItem.Type... slots) {
        ImmutableMap.Builder<ArmorItem.Type, DeferredHolder<Item, SuitItem>> builder = ImmutableMap.builder();
        for (ArmorItem.Type slot : slots) {
            SuitProperties properties = new SuitProperties(type, slot);
            consumer.accept(properties);
            properties.defense(defense.get(properties.slot)).toughness(toughness).stacksTo(1);
            builder.put(slot, register("%s_%s".formatted(type, type.equals("atrain") && slot == HELMET ? "glasses" : slot.getName()),
                () -> new SuitItem(properties), true));
        }
        return builder.build();
    }

    public static <T extends Item> DeferredHolder<Item, T> register(String id, Supplier<T> item) {
        return register(id, item, true);
    }

    public static <T extends Item> DeferredHolder<Item, T> register(String id, Supplier<T> item, boolean ownSort) {
        DeferredHolder<Item, T> registered = ITEMS.register(id, item);
        if (ownSort) {
            ITEMS_TAB.add(() -> new ItemStack(registered.get()));
        }
        return registered;
    }

    public static void init(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        modEventBus.addListener(TBItems::buildCreativeTabContents);
    }

    private static void buildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == THE_BOYS_TAB.getKey()) {
            ITEMS_TAB.forEach(stack -> event.accept(stack.get()));
            event.accept(SYRINGE.get());
            event.accept(VIAL.get());
            event.accept(VialItem.compoundV());
            for (ResourceLocation resourceLocation : Superpower.REGISTRY.keySet()) {
                if (resourceLocation.getNamespace().equals(TheBoys.MODID)) {
                    ItemStack pStack = new ItemStack(VIAL.get());
                    pStack.set(TBDataComponents.SUPERPOWER.get(), resourceLocation.toString());
                    event.accept(pStack);
                }
            }
        }
    }
}
