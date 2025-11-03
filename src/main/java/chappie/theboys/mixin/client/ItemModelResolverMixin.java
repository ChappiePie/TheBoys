package chappie.theboys.mixin.client;

import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.util.interfaces.IItemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ItemOwner;
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
    public abstract void updateForTopItem(ItemStackRenderState renderState, ItemStack stack, ItemDisplayContext displayContext, @Nullable Level level, @Nullable ItemOwner owner, int seed);

    @Inject(method = "appendItemLayers",
            at = @At("TAIL"))
    private void trySetStackForRender(ItemStackRenderState renderState, ItemStack stack, ItemDisplayContext displayContext, Level level, ItemOwner owner, int seed, CallbackInfo ci) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || !equippable.slot().isArmor()) {
            return;
        }

        if (renderState instanceof IItemRenderState state) {
            ItemStack suitStack = stack.getOrDefault(TBDataComponents.SUIT, ItemStack.EMPTY);
            if (!suitStack.isEmpty() && suitStack.getItem() instanceof SuitItem item && equippable.slot().equals(item.properties.getSlot())) {
                this.updateForTopItem(state.theBoys$getRenderState(), suitStack, ItemDisplayContext.HEAD, level, owner, seed);
            }
        }
    }
}
