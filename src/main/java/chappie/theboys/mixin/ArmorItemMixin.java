package chappie.theboys.mixin;

import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.util.tooltip.ArmorTooltip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
            if (pStack.getOrCreateTag().contains("Suit")) {
                if (pOther.isEmpty()) {
                    CompoundTag tag = pStack.getOrCreateTag().getCompound("Suit");
                    ItemStack suitItem = ItemStack.of(tag.getCompound("Tags"));
                    pAccess.set(suitItem);
                    pStack.getOrCreateTag().remove("Suit");
                    return true;
                } else if (pOther.getItem() instanceof SuitItem) {
                    if (pOther.getCount() < pOther.getMaxStackSize()) {
                        CompoundTag tag = pStack.getOrCreateTag().getCompound("Suit");
                        ItemStack suitItem = ItemStack.of(tag.getCompound("Tags"));
                        if (ItemStack.isSameItemSameTags(suitItem, pOther)) {
                            pOther.grow(1);
                        }
                        pStack.getOrCreateTag().remove("Suit");
                        return true;
                    } else {
                        ItemStack suitItem = ItemStack.of(pStack.getOrCreateTag().getCompound("Suit").getCompound("Tags"));
                        if (suitItem.getItem() != pOther.getItem()) {
                            pAccess.set(suitItem);
                            CompoundTag tag = new CompoundTag();
                            tag.put("Tags", pOther.copyWithCount(1).save(new CompoundTag()));
                            pStack.getOrCreateTag().put("Suit", tag);
                            pOther.shrink(1);
                            return true;
                        }
                    }
                }
            } else {
                if (pOther.getItem() instanceof SuitItem item) {
                    EquipmentSlot slot = item.equipmentSlot(pStack, pOther, pPlayer);
                    if (slot == null || LivingEntity.getEquipmentSlotForItem(pStack).equals(slot)) {
                        CompoundTag tag = new CompoundTag();
                        tag.put("Tags", pOther.copyWithCount(1).save(new CompoundTag()));
                        pStack.getOrCreateTag().put("Suit", tag);
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
        if (pStack.getOrCreateTag().contains("Suit")) {
            CompoundTag tag = pStack.getOrCreateTag().getCompound("Suit");
            return Optional.of(new ArmorTooltip(ItemStack.of(tag.getCompound("Tags"))));
        }
        return Optional.empty();
    }
}