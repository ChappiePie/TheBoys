package chappie.theboys.common.item.suit;

import chappie.theboys.TheBoys;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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

import java.util.List;
import java.util.function.Function;

public class SuitProperties extends Item.Properties {

    public static final Function<EquipmentSlot, Vector3f> BASIC_ARMOR_SCALE = (slot) -> new Vector3f(slot == EquipmentSlot.LEGS ? -0.39F : -0.89f);
    public final String type;
    private final ImmutableList.Builder<ItemAttributeModifiers.Entry> builder = ImmutableList.builder();
    public ArmorType slot;
    public Function<ItemStack, Vector3f> armorScale;
    private List<ItemAttributeModifiers.Entry> defaultModifiers;

    public SuitProperties(String type, ArmorType slot) {
        this.type = type;
        this.slot = slot;
        this.armorScale = (stack) -> BASIC_ARMOR_SCALE.apply(slot.getSlot());
    }

    public SuitProperties attributeModifier(Attribute attribute, Function<Identifier, AttributeModifier> modifierFunction) {
        this.builder.add(new ItemAttributeModifiers.Entry(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute), modifierFunction.apply(Identifier.withDefaultNamespace("armor." + this.slot.getName())), EquipmentSlotGroup.bySlot(this.slot.getSlot())));
        return this;
    }

    public SuitProperties armorScale(Function<ItemStack, Vector3f> armorScale) {
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