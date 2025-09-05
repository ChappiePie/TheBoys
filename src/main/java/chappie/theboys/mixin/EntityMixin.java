package chappie.theboys.mixin;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.FocusOnGoalAbility;
import chappie.theboys.common.ability.SpeedAbility;
import chappie.theboys.common.ability.SuperHearingAbility;
import chappie.theboys.common.ability.TranslucentAbility;
import chappie.theboys.util.interfaces.EntitySavingFields;
import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntitySavingFields {
    @Unique private final Map<String, Object> theBoys$map = Maps.newHashMap();
    @Shadow public float xRotO;
    @Shadow public float yRotO;

    @Shadow private @Nullable Entity vehicle;

    @Shadow public abstract void setYRot(float yRot);

    @Shadow public abstract void setXRot(float xRot);

    @Inject(method = "turn(DD)V", at = @At("HEAD"), cancellable = true)
    public void mixin$turn(double yRot, double xRot, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        for (FocusOnGoalAbility a : CommonUtil.listOfType(FocusOnGoalAbility.class, CommonUtil.getAbilities(entity))) {
            if (!a.dataManager.get(FocusOnGoalAbility.TARGET_ID).equals(a.entity.getId())) {
                Entity targetEntity = entity.getCommandSenderWorld().getEntity(a.dataManager.get(FocusOnGoalAbility.TARGET_ID));
                if (targetEntity == null) return;
                Vec3 target = targetEntity.getEyePosition(ClientUtil.getPartialTick());
                Vec3 eyePos = entity.getEyePosition(ClientUtil.getPartialTick());
                Vec3 vec3 = target.subtract(eyePos);

                if ((!a.hasSpeedAbility() || target.distanceTo(eyePos) > 2) && target.distanceTo(eyePos) < 40) {
                    double e = vec3.x();
                    double g = vec3.z();
                    this.setXRot((float) -Mth.atan2(vec3.y(), Math.sqrt(e * e + g * g)) * Mth.RAD_TO_DEG);
                    this.setYRot((float) -Mth.atan2(vec3.x(), vec3.z()) * Mth.RAD_TO_DEG);
                    this.setXRot(Mth.clamp(entity.getXRot(), -90.0F, 90.0F));
                    this.yRotO = entity.getYRot();
                    this.xRotO = entity.getXRot();
                    if (this.vehicle != null) {
                        this.vehicle.onPassengerTurned(entity);
                    }
                    ci.cancel();
                    break;
                }
            }
        }
    }

    @Inject(method = "updateDynamicGameEventListener(Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
    public void mixin$updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> listenerConsumer, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity.getCommandSenderWorld() instanceof ServerLevel serverLevel) {
            for (SuperHearingAbility a : CommonUtil.listOfType(SuperHearingAbility.class, CommonUtil.getAbilities(entity))) {
                listenerConsumer.accept(a.dynamicGameEventListener, serverLevel);
            }
        }
    }

    @Inject(method = "getXRot*", at = @At("TAIL"), cancellable = true)
    public void mixin$getXRot(CallbackInfoReturnable<Float> cir) {
        var map = this.theBoys$map();
        if (map.containsKey("xRot")) {
            this.xRotO = (float) map.get("xRot");
            cir.setReturnValue((float) map.get("xRot"));
        }
    }

    @Inject(method = "getYRot*", at = @At("TAIL"), cancellable = true)
    public void mixin$getYRot(CallbackInfoReturnable<Float> cir) {
        var map = this.theBoys$map();
        if (map.containsKey("yRot")) {
            this.yRotO = (float) map.get("yRot");
            cir.setReturnValue((float) map.get("yRot"));
        }
    }

    @Inject(method = "getDeltaMovement", at = @At("TAIL"), cancellable = true)
    public void mixin$getDeltaMovement(CallbackInfoReturnable<Vec3> cir) {
        var map = this.theBoys$map();
        if (map.containsKey("deltaMovement")) {
            cir.setReturnValue((Vec3) map.get("deltaMovement"));
        }
    }


    @Inject(method = "isInWater", at = @At("TAIL"), cancellable = true)
    public void mixin$isInWater(CallbackInfoReturnable<Boolean> cir) {
        var map = this.theBoys$map();
        if (map.containsKey("isInWater")) {
            cir.setReturnValue((boolean) map.get("isInWater"));
        }
    }

    @Inject(method = "isInvisible", at = @At("TAIL"), cancellable = true)
    public void mixin$isInvisible(CallbackInfoReturnable<Boolean> cir) {
        for (TranslucentAbility a : CommonUtil.listOfType(TranslucentAbility.class, CommonUtil.getAbilities((Entity) (Object) this))) {
            if (a.getAlpha(1) == 0) {
                cir.setReturnValue(true);
            }
        }
    }

    @Override
    public void theBoys$setup(Map<String, Object> map) {
        this.theBoys$map.putAll(map);
    }

    @Override
    public void theBoys$reset() {
        this.theBoys$map.clear();
    }

    @Override
    public Map<String, Object> theBoys$map() {
        return this.theBoys$map;
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
            for (SpeedAbility ability : CommonUtil.listOfType(SpeedAbility.class, CommonUtil.getAbilities(entity))) {
                boolean isMoving = entity.getKnownMovement().horizontalDistanceSqr() >= 1.0E-7D;
                if (ability.isEnabled() && isMoving) {
                    ci.cancel();
                    break;
                }
            }
        }
    }

    @WrapWithCondition(
            method = "updateInWaterStateAndDoWaterCurrentPushing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;doWaterSplashEffect()V")
    )
    private boolean cancelParticles(Entity entity) {
        for (SpeedAbility ability : CommonUtil.listOfType(SpeedAbility.class, CommonUtil.getAbilities(entity))) {
            if (ability.isEnabled() && entity.getDeltaMovement().horizontalDistanceSqr() >= 1.0E-7D) {
                return false;
            }
        }
        return true;
    }
}
