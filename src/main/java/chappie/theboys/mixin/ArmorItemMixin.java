package chappie.theboys.mixin;

import chappie.theboys.common.item.datacomponents.SuitContents;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Item.class)
public abstract class ArmorItemMixin {

    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    public void overrideOtherStackedOnMe(ItemStack pStack, ItemStack pOther, Slot pSlot,
                                         ClickAction pAction, Player pPlayer, SlotAccess pAccess,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (pStack.getItem() instanceof ArmorItem
                && pAction == ClickAction.SECONDARY
                && pSlot.allowModification(pPlayer)) {
            ItemStack suitItem = pStack.has(TBDataComponents.SUIT) ? pStack.get(TBDataComponents.SUIT).toStack() : ItemStack.EMPTY;
            if (pStack.has(TBDataComponents.SUIT)) {
                if (pOther.isEmpty()) {
                    pAccess.set(suitItem);
                    pStack.remove(TBDataComponents.SUIT);
                    cir.setReturnValue(true);
                } else if (pOther.getItem() instanceof SuitItem) {
                    if (pOther.getCount() < pOther.getMaxStackSize()) {
                        if (ItemStack.isSameItemSameComponents(suitItem, pOther)) {
                            pOther.grow(1);
                        }
                        pStack.remove(TBDataComponents.SUIT);
                        cir.setReturnValue(true);
                    } else {
                        if (suitItem.getItem() != pOther.getItem()) {
                            pAccess.set(suitItem);
                            pStack.set(TBDataComponents.SUIT, SuitContents.fromStack(pOther));
                            pOther.shrink(1);
                            cir.setReturnValue(true);
                        }
                    }
                }
            } else {
                if (pOther.getItem() instanceof SuitItem item) {
                    EquipmentSlot slot = item.equipmentSlot(pStack, pOther, pPlayer);
                    if (slot == null || pPlayer.getEquipmentSlotForItem(pStack).equals(slot)) {
                        pStack.set(TBDataComponents.SUIT, SuitContents.fromStack(pOther));
                        pOther.shrink(1);
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
    public void getTooltipImage(ItemStack stack, CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        if (stack.getItem() instanceof ArmorItem && stack.has(TBDataComponents.SUIT)) {
            cir.setReturnValue(Optional.of(new ArmorTooltip(stack.get(TBDataComponents.SUIT).toStack())));
        }
    }
}
