package chappie.theboys.mixin;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.FlightAbility;
import chappie.theboys.common.ability.WaterMiningAbility;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "getDefaultDimensions", at = @At("TAIL"), cancellable = true)
    public void mixin$getFallFlying(Pose pPose, CallbackInfoReturnable<EntityDimensions> cir) {
        Player player = (Player) (Object) this;
        if (player != null && player.isAlive()) {
            for (FlightAbility ability : CommonUtil.listOfType(FlightAbility.class, CommonUtil.getAbilities(player))) {
                if (player.isSprinting() && ability.isEnabled()) {
                    cir.setReturnValue(FlightAbility.FLIGHT_DIMENSIONS);
                }
            }
        }
    }

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    public void theBoys$getDestroySpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        Player player = (Player) (Object) this;
        for (WaterMiningAbility ability : CommonUtil.listOfType(WaterMiningAbility.class, CommonUtil.getAbilities(player))) {
            if (ability.isEnabled() && (player.isInWater() || player.isEyeInFluid(FluidTags.WATER))) {
                float f = cir.getReturnValue();

                if (player.isEyeInFluid(FluidTags.WATER)) {
                    float submergedMultiplier = (float) player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED);
                    if (submergedMultiplier > 0 && submergedMultiplier < 1.0f) {
                        f /= submergedMultiplier;
                    }
                }

                if (!player.onGround()) {
                    f *= 5.0f;
                }

                cir.setReturnValue(f);
                break;
            }
        }
    }
}
