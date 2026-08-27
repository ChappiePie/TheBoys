package chappie.theboys.mixin;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.WaterBreathingAbility;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEffectUtil.class)
public class MobEffectUtilMixin {

    @Inject(method = "hasWaterBreathing(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("RETURN"), cancellable = true)
    private static void waterBreathing(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        for (WaterBreathingAbility ability : CommonUtil.getAbilitiesByType(WaterBreathingAbility.class, entity)) {
            if (ability.isEnabled()) {
                cir.setReturnValue(true);
            }
        }
    }
}
