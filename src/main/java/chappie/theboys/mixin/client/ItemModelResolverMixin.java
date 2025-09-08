package chappie.theboys.mixin.client;

import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.util.interfaces.IItemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelResolver.class)
public abstract class ItemModelResolverMixin {

    @Shadow
    public abstract void updateForTopItem(ItemStackRenderState renderState, ItemStack stack, ItemDisplayContext displayContext, boolean leftHand, @Nullable Level level, @Nullable LivingEntity entity, int seed);

    @Inject(method = "appendItemLayers(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V",
            at = @At("TAIL"))
    private void trySetStackForRender(ItemStackRenderState renderState, ItemStack stack, ItemDisplayContext displayContext, Level level, LivingEntity entity, int seed, CallbackInfo ci) {
        if (stack.getItem() instanceof ArmorItem && renderState instanceof IItemRenderState state) {
            ItemStack suitStack = stack.getOrDefault(TBDataComponents.SUIT, ItemStack.EMPTY);
            if (!suitStack.isEmpty() && suitStack.getItem() instanceof SuitItem item) {
                Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
                if (equippable != null && equippable.slot().equals(item.properties.getSlot())) {
                    this.updateForTopItem(state.theBoys$getRenderState(), suitStack, ItemDisplayContext.HEAD, false, level, entity, seed);
                }
            }
        }
    }
}
