package chappie.theboys.common.item.suit;

import chappie.theboys.TheBoys;
import com.google.common.collect.ImmutableList;
import net.minecraft.Util;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SuitProperties extends Item.Properties {

    public static final Function<EquipmentSlot, Vector3f> BASIC_ARMOR_SCALE = (slot) -> new Vector3f(slot == EquipmentSlot.LEGS ? -0.35F : -0.79f);
    private static final EnumMap<ArmorType, UUID> ARMOR_MODIFIER_UUID_PER_TYPE = Util.make(new EnumMap<>(ArmorType.class), (map) -> {
        map.put(ArmorType.BOOTS, UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"));
        map.put(ArmorType.LEGGINGS, UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"));
        map.put(ArmorType.CHESTPLATE, UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"));
        map.put(ArmorType.HELMET, UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150"));
    });
    public final String type;
    private final ImmutableList.Builder<ItemAttributeModifiers.Entry> builder = ImmutableList.builder();
    public ArmorType slot;
    public BiFunction<HumanoidRenderState, ItemStack, Vector3f> armorScale;
    private List<ItemAttributeModifiers.Entry> defaultModifiers;

    public SuitProperties(String type, ArmorType slot) {
        this.type = type;
        this.slot = slot;
        this.armorScale = (renderState, stack) -> BASIC_ARMOR_SCALE.apply(slot.getSlot());
    }

    public SuitProperties attributeModifier(Attribute attribute, Function<UUID, AttributeModifier> modifierFunction) {
        this.builder.add(new ItemAttributeModifiers.Entry(Holder.direct(attribute), modifierFunction.apply(ARMOR_MODIFIER_UUID_PER_TYPE.get(this.slot)), EquipmentSlotGroup.bySlot(this.slot.getSlot())));
        return this;
    }

    public SuitProperties armorScale(BiFunction<HumanoidRenderState, ItemStack, Vector3f> armorScale) {
        this.armorScale = armorScale;
        return this;
    }

    public SuitProperties toughness(double toughness) {
        return this.attributeModifier(Attributes.ARMOR_TOUGHNESS.value(), (uuid) -> new AttributeModifier(TheBoys.id("suit_toughness"), toughness, AttributeModifier.Operation.ADD_VALUE));
    }

    public SuitProperties defense(double defense) {
        return this.attributeModifier(Attributes.ARMOR.value(), (uuid) -> new AttributeModifier(TheBoys.id("suit_modifier"), defense, AttributeModifier.Operation.ADD_VALUE));
    }

    public List<ItemAttributeModifiers.Entry> defaultModifiers() {
        if (this.defaultModifiers == null) {
            this.defaultModifiers = this.builder.build();
        }
        return this.defaultModifiers;
    }

    public EquipmentSlot getSlot() {
        return this.slot.getSlot();
    }

    @NotNull
    @Override
    public SuitProperties food(@NotNull FoodProperties pFood) {
        return (SuitProperties) super.food(pFood);
    }

    @NotNull
    @Override
    public SuitProperties stacksTo(int pMaxStackSize) {
        return (SuitProperties) super.stacksTo(pMaxStackSize);
    }

    @NotNull
    @Override
    public SuitProperties durability(int pMaxDamage) {
        return (SuitProperties) super.durability(pMaxDamage);
    }

    @NotNull
    @Override
    public SuitProperties craftRemainder(@NotNull Item pCraftingRemainingItem) {
        return (SuitProperties) super.craftRemainder(pCraftingRemainingItem);
    }

    @NotNull
    @Override
    public SuitProperties rarity(@NotNull Rarity pRarity) {
        return (SuitProperties) super.rarity(pRarity);
    }

    @NotNull
    @Override
    public SuitProperties fireResistant() {
        return (SuitProperties) super.fireResistant();
    }

    @NotNull
    @Override
    public SuitProperties requiredFeatures(FeatureFlag @NotNull ... pRequiredFeatures) {
        return (SuitProperties) super.requiredFeatures(pRequiredFeatures);
    }
}
