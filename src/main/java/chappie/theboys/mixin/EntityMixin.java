package chappie.theboys.mixin;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.FishSwarmAbility;
import chappie.theboys.common.ability.SpeedAbility;
import chappie.theboys.common.ability.SuperHearingAbility;
import chappie.theboys.common.ability.TranslucentAbility;
import chappie.theboys.util.interfaces.ILivingEntityEx;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiConsumer;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "updateDynamicGameEventListener(Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
    public void mixin$updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> listenerConsumer, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity.level() instanceof ServerLevel serverLevel) {
            for (SuperHearingAbility a : CommonUtil.getAbilitiesByType(SuperHearingAbility.class, entity)) {
                listenerConsumer.accept(a.dynamicGameEventListener, serverLevel);
            }
        }
    }

    @Inject(method = "updateSwimming()V", at = @At("HEAD"), cancellable = true)
    public void mixin$updateSwimming(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        for (FishSwarmAbility a : CommonUtil.getAbilitiesByType(FishSwarmAbility.class, entity)) {
            if (a.isEnabled()) {
                ci.cancel();
                if (entity.isSwimming()) {
                    entity.setSwimming(false);
                }
            }
        }
    }

    @Inject(method = "isShiftKeyDown()Z", at = @At("HEAD"), cancellable = true)
    public void mixin$isShiftKeyDown(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        for (SpeedAbility a : CommonUtil.getAbilitiesByType(SpeedAbility.class, entity)) {
            if (a.isEnabled()) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "isInvisible", at = @At("TAIL"), cancellable = true)
    public void mixin$isInvisible(CallbackInfoReturnable<Boolean> cir) {
        for (TranslucentAbility a : CommonUtil.getAbilitiesByType(TranslucentAbility.class, (Entity) (Object) this)) {
            if (a.getAlpha(1) == 0) {
                cir.setReturnValue(true);
            }
        }
    }

    //-----------------------------------------------------------------------

    @Inject(method = "setSprinting", at = @At("TAIL"))
    public void updateShape(boolean pSprinting, CallbackInfo ci) {
        ((Entity) (Object) this).refreshDimensions();
    }

    @Inject(method = "setSwimming", at = @At("HEAD"), cancellable = true)
    public void mixin$setSwimming(boolean pSwimming, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (pSwimming) {
            for (SpeedAbility ability : CommonUtil.getAbilitiesByType(SpeedAbility.class, entity)) {
                boolean isMoving = theBoys$isMoving(entity);
                if (ability.isEnabled() && isMoving) {
                    ci.cancel();
                    break;
                }
            }
        }
    }

    @WrapWithCondition(
            method = "updateFluidInteraction",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;doWaterSplashEffect()V")
    )
    private boolean cancelParticles(Entity entity) {
        for (SpeedAbility ability : CommonUtil.getAbilitiesByType(SpeedAbility.class, entity)) {
            if (ability.isEnabled() && theBoys$isMoving(entity)) {
                return false;
            }
        }
        return true;
    }

    @Unique
    private boolean theBoys$isMoving(Entity entity) {
        if (entity.level().isClientSide()) {
            return entity.getDeltaMovement().horizontalDistanceSqr() >= 1.0E-7D;
        }
        if (entity instanceof ILivingEntityEx ex) {
            double dx = entity.getX() - ex.theBoys$oldPos().x;
            double dz = entity.getZ() - ex.theBoys$oldPos().z;
            return dx * dx + dz * dz >= 1.0E-7D;
        }
        return false;
    }
}
