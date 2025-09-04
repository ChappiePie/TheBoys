package chappie.theboys.mixin.client;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.FocusOnGoalAbility;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends LivingEntity {

    protected LocalPlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "getViewYRot", at = @At("TAIL"), cancellable = true)
    public void mixin$getViewYRot(float partialTick, CallbackInfoReturnable<Float> cir) {
        for (FocusOnGoalAbility a : CommonUtil.listOfType(FocusOnGoalAbility.class, CommonUtil.getAbilities(this))) {
            float f = a.getYRot(cir.getReturnValue(), partialTick);
            if (cir.getReturnValue() != f) {
                cir.setReturnValue(f);
            }
            break;
        }
    }

    @Inject(method = "getViewXRot", at = @At("TAIL"), cancellable = true)
    public void mixin$getViewXRot(float partialTick, CallbackInfoReturnable<Float> cir) {
        for (FocusOnGoalAbility a : CommonUtil.listOfType(FocusOnGoalAbility.class, CommonUtil.getAbilities(this))) {
            float f = a.getXRot(cir.getReturnValue(), partialTick);
            if (cir.getReturnValue() != f) {
                cir.setReturnValue(f);
            }
            break;
        }
    }
}
