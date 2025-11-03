package chappie.theboys.mixin;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.FlightAbility;
import chappie.theboys.common.ability.SpeedAbility;
import chappie.theboys.util.interfaces.EntitySavingFields;
import chappie.theboys.util.interfaces.ILivingEntityEx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ILivingEntityEx {

    @Unique
    private Vec3 oldPos = Vec3.ZERO;

    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void theBoys$setupOldPos(Vec3 pos) {
        this.oldPos = pos;
    }

    @Override
    public Vec3 theBoys$oldPos() {
        return this.oldPos;
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    public void mixin$tick(CallbackInfo ci) {
        this.oldPos = new Vec3(this.position().toVector3f());
    }

    @Inject(method = "maxUpStep()F", at = @At("RETURN"), cancellable = true)
    public void mixin$maxUpStep(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        for (SpeedAbility a : CommonUtil.listOfType(SpeedAbility.class, CommonUtil.getAbilities(entity))) {
            if (a.isEnabled() && !entity.isSwimming() && !entity.isFallFlying()) {
                cir.setReturnValue(cir.getReturnValue() + 1);
                break;
            }
        }
    }

    @Inject(method = "getDefaultDimensions", at = @At("TAIL"), cancellable = true)
    public void mixin$getFallFlying(Pose pPose, CallbackInfoReturnable<EntityDimensions> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity != null && entity.isAlive() && entity instanceof Player) {
            for (FlightAbility ability : CommonUtil.listOfType(FlightAbility.class, CommonUtil.getAbilities(entity))) {
                if (entity.isSprinting() && ability.isEnabled()) {
                    cir.setReturnValue(FlightAbility.FLIGHT_DIMENSIONS);
                }
            }
        }
    }

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    public void mixin$causeFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level() instanceof ServerLevel level) {
            for (FlightAbility a : CommonUtil.listOfType(FlightAbility.class, CommonUtil.getAbilities(entity))) {
                if (a.causeFallDamage(level, entity, fallDistance)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @Inject(method = "isFallFlying", at = @At("TAIL"), cancellable = true)
    public void mixin$getFallFlying(CallbackInfoReturnable<Boolean> cir) {
        var map = ((EntitySavingFields) this).theBoys$map();
        if (map.containsKey("isFallFlying")) {
            cir.setReturnValue((boolean) map.get("isFallFlying"));
        }
    }

    @Inject(method = "getSwimAmount", at = @At("TAIL"), cancellable = true)
    public void mixin$getSwimAmount(CallbackInfoReturnable<Float> cir) {
        var map = ((EntitySavingFields) this).theBoys$map();
        if (map.containsKey("swimAmount")) {
            cir.setReturnValue((float) map.get("swimAmount"));
        }
    }

    @Inject(method = "isVisuallySwimming", at = @At("TAIL"), cancellable = true)
    public void mixin$isVisuallySwimming(CallbackInfoReturnable<Boolean> cir) {
        var map = ((EntitySavingFields) this).theBoys$map();
        if (map.containsKey("isVisuallySwimming")) {
            cir.setReturnValue((boolean) map.get("isVisuallySwimming"));
        }
    }

    @Inject(method = "getFallFlyingTicks", at = @At("TAIL"), cancellable = true)
    public void mixin$getFallFlyingTicks(CallbackInfoReturnable<Integer> cir) {
        var map = ((EntitySavingFields) this).theBoys$map();
        if (map.containsKey("fallFlyingTicks")) {
            cir.setReturnValue((int) map.get("fallFlyingTicks"));
        }
    }
}
