package chappie.theboys.mixin.client;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.ParkourAbility;
import chappie.theboys.common.ability.parkour.SlideHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Unique
    private float theBoys$slideMultiplier = 1.0F;

    @Unique
    private float theBoys$savedWalkDist = 0.0F;

    @Unique
    private float theBoys$savedWalkDistO = 0.0F;

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void theBoys$updateSlideMultiplier(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;

        for (ParkourAbility ability : CommonUtil.listOfType(ParkourAbility.class, CommonUtil.getAbilities(player))) {
            if (ability.slideHandler.isActive()) {
                int ticks = ability.dataManager.get(SlideHandler.SLIDE_TICKS);
                boolean isStopping = ability.dataManager.get(SlideHandler.IS_STOPPING);
                if (isStopping) {
                    this.theBoys$slideMultiplier = Math.min(1.0F, this.theBoys$slideMultiplier + 0.125F);
                } else if (ticks < 5) {
                    this.theBoys$slideMultiplier = 1.0F - (ticks / 5.0F);
                } else {
                    this.theBoys$slideMultiplier = 0.0F;
                }
                return;
            }
        }
        this.theBoys$slideMultiplier = 1.0F;
    }

    @Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift = At.Shift.AFTER))
    private void theBoys$applySlideMultiplier(CallbackInfo ci) {
        if (this.theBoys$slideMultiplier < 1.0F) {
            LocalPlayer player = (LocalPlayer) (Object) this;
            player.walkDist = theBoys$savedWalkDist + (player.walkDist - theBoys$savedWalkDist) * this.theBoys$slideMultiplier;
            player.walkDistO = theBoys$savedWalkDistO + (player.walkDistO - theBoys$savedWalkDistO) * this.theBoys$slideMultiplier;
        }
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void theBoys$saveWalkDist(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        this.theBoys$savedWalkDist = player.walkDist;
        this.theBoys$savedWalkDistO = player.walkDistO;
    }
}
