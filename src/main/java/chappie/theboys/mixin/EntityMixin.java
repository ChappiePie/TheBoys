package chappie.theboys.mixin;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.FishSwarmAbility;
import chappie.theboys.common.ability.SpeedAbility;
import chappie.theboys.common.ability.SuperHearingAbility;
import chappie.theboys.common.ability.TranslucentAbility;
import chappie.theboys.util.interfaces.EntitySavingFields;
import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.phys.Vec3;
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
    @Unique
    private final Map<String, Object> theBoys$map = Maps.newHashMap();
    @Shadow
    public float xRotO;
    @Shadow
    public float yRotO;

    @Inject(method = "updateDynamicGameEventListener(Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
    public void mixin$updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> listenerConsumer, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity.level() instanceof ServerLevel serverLevel) {
            for (SuperHearingAbility a : CommonUtil.listOfType(SuperHearingAbility.class, CommonUtil.getAbilities(entity))) {
                listenerConsumer.accept(a.dynamicGameEventListener, serverLevel);
            }
        }
    }

    @Inject(method = "updateSwimming()V", at = @At("HEAD"), cancellable = true)
    public void mixin$updateSwimming(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        for (FishSwarmAbility a : CommonUtil.listOfType(FishSwarmAbility.class, CommonUtil.getAbilities(entity))) {
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
        for (SpeedAbility a : CommonUtil.listOfType(SpeedAbility.class, CommonUtil.getAbilities(entity))) {
            if (a.isEnabled()) {
                cir.setReturnValue(false);
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
