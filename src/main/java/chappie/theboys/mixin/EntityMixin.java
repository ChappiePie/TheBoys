package chappie.theboys.mixin;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.DamageImmunityAbility;
import chappie.theboys.common.ability.SpeedAbility;
import chappie.theboys.common.ability.TranslucentAbility;
import chappie.theboys.util.interfaces.EntitySavingFields;
import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Entity.class)
public class EntityMixin implements EntitySavingFields {
    @Shadow public float xRotO;
    @Shadow public float yRotO;
    @Unique
    private final Map<String, Object> theBoys$map = Maps.newHashMap();

    @Inject(method = "isInvulnerableTo(Lnet/minecraft/world/damagesource/DamageSource;)Z", at = @At("TAIL"), cancellable = true)
    public void mixin$isInvulnerableTo(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            Entity entity = (Entity) (Object) this;
            for (DamageImmunityAbility a : CommonUtil.listOfType(DamageImmunityAbility.class, CommonUtil.getAbilities(entity))) {
                for (String s : a.damageSources) {
                    if (s.equals(source.getMsgId()) && a.isEnabled()) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    @Inject(method = "getXRot", at = @At("TAIL"), cancellable = true)
    public void mixin$getXRot(CallbackInfoReturnable<Float> cir) {
        var map = ((EntitySavingFields) this).map();
        if (map.containsKey("xRot")) {
            this.xRotO = (float) map.get("xRot");
            cir.setReturnValue((float) map.get("xRot"));
        }
    }

    @Inject(method = "getYRot", at = @At("TAIL"), cancellable = true)
    public void mixin$getYRot(CallbackInfoReturnable<Float> cir) {
        var map = ((EntitySavingFields) this).map();
        if (map.containsKey("yRot")) {
            this.yRotO = (float) map.get("yRot");
            cir.setReturnValue((float) map.get("yRot"));
        }
    }

    @Inject(method = "getDeltaMovement", at = @At("TAIL"), cancellable = true)
    public void mixin$getDeltaMovement(CallbackInfoReturnable<Vec3> cir) {
        var map = ((EntitySavingFields) this).map();
        if (map.containsKey("deltaMovement")) {
            cir.setReturnValue((Vec3) map.get("deltaMovement"));
        }
    }


    @Inject(method = "isInWater", at = @At("TAIL"), cancellable = true)
    public void mixin$isInWater(CallbackInfoReturnable<Boolean> cir) {
        var map = ((EntitySavingFields) this).map();
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
    public void setup(Map<String, Object> map) {
        this.theBoys$map.putAll(map);
    }

    @Override
    public void reset() {
        this.theBoys$map.clear();
    }

    @Override
    public Map<String, Object> map() {
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
                float walkDifference = (entity.walkDist / 0.6F) - (entity.walkDistO / 0.6F);
                boolean isMoving = entity.getDeltaMovement().horizontalDistanceSqr() >= 1.0E-7D;
                if (ability.isEnabled() && (isMoving || walkDifference > 0.0F)) {
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
