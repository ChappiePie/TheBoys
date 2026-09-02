package chappie.theboys.mixin;

import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.common.item.suit.SuitItem;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
    public void mixin$forEach(EquipmentSlot equipmentSlot, BiConsumer<Holder<Attribute>, AttributeModifier> action, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        ItemStack suitStack = stack.has(TBDataComponents.SUIT) ? stack.get(TBDataComponents.SUIT).toStack() : ItemStack.EMPTY;
        if (suitStack.getItem() instanceof SuitItem item) {
            for (ItemAttributeModifiers.Entry entry : item.properties.defaultModifiers()) {
                if (entry.slot().test(equipmentSlot)) {
                    action.accept(entry.attribute(), entry.modifier());
                }
            }
        }
    }
}
