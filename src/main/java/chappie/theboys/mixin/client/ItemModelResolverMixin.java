package chappie.theboys.mixin.client;

import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.util.interfaces.SuitOverlayHolder;
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

    @Inject(method = "appendItemLayers", at = @At("TAIL"))
    private void theBoys$appendSuitOverlay(ItemStackRenderState renderState, ItemStack stack, ItemDisplayContext displayContext, Level level, ItemOwner owner, int seed, CallbackInfo ci) {
        ItemStackRenderState.LayerRenderState[] baseLayers = ((ItemStackRenderStateAccessor) renderState).theBoys$getLayers();

        ItemStack suitStack = stack.getOrDefault(TBDataComponents.SUIT, ItemStack.EMPTY);
        renderState.appendModelIdentityElement(suitStack);

        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if ((equippable == null || !equippable.slot().isArmor()) || suitStack.isEmpty() || !(suitStack.getItem() instanceof SuitItem suitItem) || !equippable.slot().equals(suitItem.properties.getSlot())) {
            ((SuitOverlayHolder) baseLayers[0]).theBoys$getSuitOverlay().clear();
        } else {
            this.updateForTopItem(((SuitOverlayHolder) baseLayers[0]).theBoys$getSuitOverlay(), suitStack, ItemDisplayContext.HEAD, level, owner, seed);
        }
    }
}