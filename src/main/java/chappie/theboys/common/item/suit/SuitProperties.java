package chappie.theboys.common.item.suit;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SuitProperties extends Item.Properties {

    public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
        @Override
        protected @NotNull ItemStack execute(BlockSource pSource, @NotNull ItemStack pStack) {
            BlockPos blockpos = pSource.pos().relative(pSource.state().getValue(DispenserBlock.FACING));
            List<LivingEntity> list = pSource.level().getEntitiesOfClass(LivingEntity.class, new AABB(blockpos), EntitySelector.NO_SPECTATORS.and(new EntitySelector.MobCanWearArmorEntitySelector(pStack)));
            if (!list.isEmpty() && pStack.getItem() instanceof SuitItem item) {
                ItemStack armorStack = list.get(0).getItemBySlot(item.properties.getSlot());
                if (armorStack.getItem() instanceof ArmorItem) {
                    CompoundTag tag = new CompoundTag();
                    tag.put("Tags", pStack.copyWithCount(1).save(new CompoundTag()));
                    armorStack.getOrCreateTag().put("Suit", tag);
                    pStack.shrink(1);
                    return pStack;
                }
            }
            return super.execute(pSource, pStack);
        }
    };
    public static final Function<EquipmentSlot, Vector3f> BASIC_ARMOR_SCALE = (slot) -> new Vector3f(slot == EquipmentSlot.LEGS ? -0.35F : -0.79f);
    private static final EnumMap<ArmorItem.Type, UUID> ARMOR_MODIFIER_UUID_PER_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), (map) -> {
        map.put(ArmorItem.Type.BOOTS, UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"));
        map.put(ArmorItem.Type.LEGGINGS, UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"));
        map.put(ArmorItem.Type.CHESTPLATE, UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"));
        map.put(ArmorItem.Type.HELMET, UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150"));
    });
    public final String type;
    private final ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
    public ArmorItem.Type slot;
    public BiFunction<LivingEntity, ItemStack, Vector3f> armorScale;
    private Multimap<Attribute, AttributeModifier> defaultModifiers;

    public SuitProperties(String type, ArmorItem.Type slot) {
        this.type = type;
        this.slot = slot;
        this.armorScale = (entity, stack) -> BASIC_ARMOR_SCALE.apply(slot.getSlot());
    }

    public SuitProperties attributeModifier(Attribute attribute, Function<UUID, AttributeModifier> modifierFunction) {
        this.builder.put(attribute, modifierFunction.apply(ARMOR_MODIFIER_UUID_PER_TYPE.get(this.slot)));
        return this;
    }

    public SuitProperties armorScale(BiFunction<LivingEntity, ItemStack, Vector3f> armorScale) {
        this.armorScale = armorScale;
        return this;
    }

    public SuitProperties toughness(double toughness) {
        return this.attributeModifier(Attributes.ARMOR_TOUGHNESS, (uuid) -> new AttributeModifier(uuid, "Suit toughness", toughness, AttributeModifier.Operation.ADDITION));
    }

    public SuitProperties defense(double defense) {
        return this.attributeModifier(Attributes.ARMOR, (uuid) -> new AttributeModifier(uuid, "Suit modifier", defense, AttributeModifier.Operation.ADDITION));
    }

    public Multimap<Attribute, AttributeModifier> defaultModifiers() {
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
    public SuitProperties defaultDurability(int pMaxDamage) {
        return (SuitProperties) super.defaultDurability(pMaxDamage);
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
