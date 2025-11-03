package chappie.theboys.mixin;

import chappie.theboys.common.capability.TheBoysCap;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public class InventoryMixin {

    @Inject(method = "setSelectedSlot", at = @At("HEAD"), cancellable = true)
    public void cancelSwapSlots$setSelectedHotbarSlot(int index, CallbackInfo ci) {
        Inventory inv = (Inventory) (Object) this;
        TheBoysCap cap = TheBoysCap.getCap(inv.player);
        if (cap != null && cap.vialAnim.timeline.value(1) > 0) {
            ci.cancel();
        }
    }
}
