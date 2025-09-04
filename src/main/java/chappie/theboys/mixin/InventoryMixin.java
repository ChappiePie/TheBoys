package chappie.theboys.mixin;

import chappie.theboys.common.capability.TheBoysCap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public class InventoryMixin {

    @Inject(method = "swapPaint(D)V", at = @At("HEAD"), cancellable = true)
    public void cancelSwapSlots(double pDirection, CallbackInfo ci) {
        assert Minecraft.getInstance().player != null;
        TheBoysCap cap = TheBoysCap.getCap(Minecraft.getInstance().player);
        if (cap != null && cap.vialAnim.rollVial.value(1) > 0) {
            ci.cancel();
        }
    }
}
