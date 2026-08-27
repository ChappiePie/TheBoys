package chappie.theboys.mixin.client;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.ParkourAbility;
import chappie.theboys.common.ability.parkour.SlideHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientAvatarState.class)
public class ClientAvatarStateMixin {

    @Shadow private float bob;
    @Shadow private float bobO;

    @Unique
    private float slideMultiplier = 1.0F;

    @Inject(method = "updateBob", at = @At("RETURN"))
    private void theBoys$applySlideMultiplier(float bob, CallbackInfo ci) {
        this.bob *= this.slideMultiplier;
        this.bobO *= this.slideMultiplier;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void theBoys$updateMultiplier(CallbackInfo ci) {
        AbstractClientPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        for (ParkourAbility ability : CommonUtil.getAbilitiesByType(ParkourAbility.class, player)) {
            if (ability.slideHandler.isActive()) {
                int ticks = ability.dataManager.get(SlideHandler.SLIDE_TICKS);
                boolean isStopping = ability.dataManager.get(SlideHandler.IS_STOPPING);
                if (isStopping) {
                    this.slideMultiplier = Math.min(1.0F, this.slideMultiplier + 0.125F);
                } else if (ticks < 5) {
                    this.slideMultiplier = 1.0F - (ticks / 5.0F);
                } else {
                    this.slideMultiplier = 0.0F;
                }
                return;
            }
        }
        this.slideMultiplier = 1.0F;
    }
}
