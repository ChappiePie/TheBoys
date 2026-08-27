package chappie.theboys.mixin;

import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.data.CommonAccessors;
import chappie.theboys.common.ability.DolphinCompanionAbility;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Dolphin.class)
public class DolphinMixin {

    @Inject(method = "tick", at = @At("RETURN"))
    public void canUse(CallbackInfo ci) {
        Dolphin dolphin = (Dolphin) (Object) this;
        for (Player player : dolphin.level().getEntitiesOfClass(Player.class,
                CommonUtil.boxWithRange(dolphin.position(), 200))) {
            for (DolphinCompanionAbility a : CommonUtil.getAbilitiesByType(DolphinCompanionAbility.class, player)) {
                double v = dolphin.distanceTo(player);
                if (a.isEnabled() && v < a.dataManager.get(CommonAccessors.DISTANCE)) {
                    dolphin.getLookControl().setLookAt(player, (float) (dolphin.getMaxHeadYRot() + 20), (float) dolphin.getMaxHeadXRot());
                    if (dolphin.distanceToSqr(player) < 6.25) {
                        dolphin.getNavigation().stop();
                    } else {
                        dolphin.getNavigation().moveTo(player, 8);
                    }
                }
            }
        }
    }
}
