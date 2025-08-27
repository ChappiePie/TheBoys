package chappie.theboys.mixin;

import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.util.tooltip.ArmorTooltip;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(ArmorItem.class)
public abstract class ArmorItemMixin extends Item {

    public ArmorItemMixin(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack pStack, ItemStack pOther, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess) {
        if (pAction == ClickAction.SECONDARY && pSlot.allowModification(pPlayer)) {
            ItemStack suitItem = pStack.getOrDefault(TBDataComponents.SUIT, ItemStack.EMPTY);
            if (!suitItem.isEmpty()) {
                if (pOther.isEmpty()) {
                    pAccess.set(suitItem);
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