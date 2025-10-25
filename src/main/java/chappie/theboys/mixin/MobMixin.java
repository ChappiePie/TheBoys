package chappie.theboys.mixin;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.FishSwarmAbility;
import chappie.theboys.util.TBCommonUtil;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mob.class)
public class MobMixin {

    @WrapWithCondition(
            method = "serverAiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/control/MoveControl;tick()V")
    )
    private boolean aiStep(MoveControl instance) {
        if (((Mob) (Object) this) instanceof AbstractSchoolingFish fish) {
            for (Player player : fish.level().getEntitiesOfClass(Player.class,
                    CommonUtil.boxWithRange(fish.position(), FishSwarmAbility.DETECTION_RADIUS))) {
                for (FishSwarmAbility a : CommonUtil.listOfType(FishSwarmAbility.class,
                        CommonUtil.getAbilities(player))) {
                    if (a.isEnabled() && fish.position().distanceTo(player.position()) >= a.dataManager.get(TBCommonUtil.DISTANCE)) {
                        if (fish.isEyeInFluid(FluidTags.WATER)) {
                            fish.setDeltaMovement(fish.getDeltaMovement().add(0.0, 0.005, 0.0));
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
