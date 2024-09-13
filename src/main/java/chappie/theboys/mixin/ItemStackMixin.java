package chappie.theboys.mixin;

import chappie.theboys.common.item.suit.SuitItem;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(at = @At("RETURN"), method = "getAttributeModifiers(Lnet/minecraft/world/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;", cancellable = true)
    private void modifyAttributesForSuit(EquipmentSlot slot, CallbackInfoReturnable<Multimap<Attribute, AttributeModifier>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == slot) {
            if (stack.getOrCreateTag().contains("Suit")) {
                CompoundTag tag = stack.getOrCreateTag().getCompound("Suit");
                if (ItemStack.of(tag.getCompound("Tags")).getItem() instanceof SuitItem item
                        && armorItem.getEquipmentSlot() == item.properties.getSlot()) {
                    Multimap<Attribute, AttributeModifier> map = cir.getReturnValue();
                    map.putAll(item.properties.defaultModifiers());
                    cir.setReturnValue(map);
                }
            }
        }
    }
}