package chappie.theboys.mixin.client;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.ParkourAbility;
import chappie.theboys.common.ability.SpeedAbility;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Inject(method = "isShiftKeyDown()Z", at = @At("HEAD"), cancellable = true)
    public void mixin$isShiftKeyDown(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer entity = (LocalPlayer) (Object) this;
        for (SpeedAbility a : CommonUtil.getAbilitiesByType(SpeedAbility.class, entity)) {
            if (a.isEnabled()) {
                cir.setReturnValue(false);
            }
        }
        for (ParkourAbility a : CommonUtil.getAbilitiesByType(ParkourAbility.class, entity)) {
            if (a.slideHandler.isActive()) {
                cir.setReturnValue(false);
            }
        }
    }
}
