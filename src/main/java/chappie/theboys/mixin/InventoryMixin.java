package chappie.theboys.mixin;

import chappie.theboys.common.capability.TheBoysCap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public class InventoryMixin {

    @Shadow @Final public Player player;

    @Inject(method = "swapPaint(D)V", at = @At("HEAD"), cancellable = true)
    public void cancelSwapSlots(double pDirection, CallbackInfo ci) {
        TheBoysCap cap = TheBoysCap.getCap(this.player);
        if (cap != null && cap.vialAnim.rollVial.value(1) > 0) {
            ci.cancel();
        }
    }
}
