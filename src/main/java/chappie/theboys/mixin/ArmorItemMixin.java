package chappie.theboys.mixin;

import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.util.tooltip.ArmorTooltip;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(ArmorItem.class)
public abstract class ArmorItemMixin extends Item {

    public ArmorItemMixin(Properties pProperties) {
        super(pProperties);
    }

    /*
    TODO Find out the solution to add attributes when setting up a suit on armor
     */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack pStack, ItemStack pOther, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess) {
        if (pAction == ClickAction.SECONDARY && pSlot.allowModification(pPlayer)) {
            ItemStack suitItem = pStack.getOrDefault(TBDataComponents.SUIT, ItemStack.EMPTY);
            if (!suitItem.isEmpty()) {
                if (pOther.isEmpty()) {
                    pAccess.set(suitItem);
                    pStack.update(
                            DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY, itemAttributeModifiers -> {
                                if (suitItem.getItem() instanceof SuitItem item) {
                                    ImmutableList.Builder<ItemAttributeModifiers.Entry> builder = ImmutableList.builderWithExpectedSize(itemAttributeModifiers.modifiers().size() + 1);
//                                    for (ItemAttributeModifiers.Entry entry : itemAttributeModifiers.modifiers()) {
//                                        if (!item.properties.defaultModifiers().contains(entry)) {
//                                            builder.add(entry);
//                                        }
//                                    }
                                    return new ItemAttributeModifiers(builder.build(), itemAttributeModifiers.showInTooltip);
                                }
                                return itemAttributeModifiers;
                            }
                    );
                    pStack.remove(TBDataComponents.SUIT);
                    return true;
                } else if (pOther.getItem() instanceof SuitItem) {
                    if (pOther.getCount() < pOther.getMaxStackSize()) {
                        if (ItemStack.isSameItemSameComponents(suitItem, pOther)) {
                            pOther.grow(1);
                        }
                        pStack.remove(TBDataComponents.SUIT);
                        return true;
                    } else {
                        if (suitItem.getItem() != pOther.getItem()) {
                            pAccess.set(suitItem);
                            pStack.set(TBDataComponents.SUIT, pOther.copyWithCount(1));

//                            pStack.update(
//                                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY, itemAttributeModifiers -> {
//                                        ItemAttributeModifiers modifiers = itemAttributeModifiers;
//                                        if (suitItem.getItem() instanceof SuitItem item) {
//                                            for (ItemAttributeModifiers.Entry defaultModifier : item.properties.defaultModifiers()) {
//                                                modifiers = modifiers.withModifierAdded(defaultModifier.attribute(), defaultModifier.modifier(), defaultModifier.slot());
//                                            }
//                                        }
//                                        return modifiers;
//                                    }
//                            );


                            pOther.shrink(1);
                            return true;
                        }
                    }
                }
            } else {
                if (pOther.getItem() instanceof SuitItem item) {
                    EquipmentSlot slot = item.equipmentSlot(pStack, pOther, pPlayer);
                    if (slot == null || pPlayer.getEquipmentSlotForItem(pStack).equals(slot)) {
                        pStack.set(TBDataComponents.SUIT, pOther.copyWithCount(1));
//                        pStack.update(
//                                DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY, itemAttributeModifiers -> {
//                                    ItemAttributeModifiers modifiers = itemAttributeModifiers;
//                                        for (ItemAttributeModifiers.Entry defaultModifier : item.properties.defaultModifiers()) {
//                                            modifiers = modifiers.withModifierAdded(defaultModifier.attribute(), defaultModifier.modifier(), defaultModifier.slot());
//                                        }
//                                    return modifiers;
//                                }
//                        );
                        pOther.shrink(1);
                        return true;
                    }
                }
            }
        }
        return super.overrideOtherStackedOnMe(pStack, pOther, pSlot, pAction, pPlayer, pAccess);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        ItemStack suitItem = pStack.getOrDefault(TBDataComponents.SUIT, ItemStack.EMPTY);
        if (!suitItem.isEmpty()) {
            return Optional.of(new ArmorTooltip(suitItem));
        }
        return Optional.empty();
    }
}