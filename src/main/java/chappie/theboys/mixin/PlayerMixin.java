package chappie.theboys.mixin;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.FlightAbility;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
                    cir.setReturnValue(EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.51F));
                }
            }
        }
    }
}
