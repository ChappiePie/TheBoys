package chappie.theboys.mixin;

import chappie.theboys.util.EntitySavingFields;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "isFallFlying", at = @At("TAIL"), cancellable = true)
    public void mixin$getFallFlying(CallbackInfoReturnable<Boolean> cir) {
        var map = ((EntitySavingFields) this).map();
        if (map.containsKey("isFallFlying")) {
            cir.setReturnValue((boolean) map.get("isFallFlying"));
        } else {
            boolean b = false;
        }
    }

    @Inject(method = "getSwimAmount", at = @At("TAIL"), cancellable = true)
    public void mixin$getSwimAmount(CallbackInfoReturnable<Float> cir) {
        var map = ((EntitySavingFields) this).map();
        if (map.containsKey("swimAmount")) {
            cir.setReturnValue((float) map.get("swimAmount"));
        }
    }

    @Inject(method = "isVisuallySwimming", at = @At("TAIL"), cancellable = true)
    public void mixin$isVisuallySwimming(CallbackInfoReturnable<Boolean> cir) {
        var map = ((EntitySavingFields) this).map();
        if (map.containsKey("isVisuallySwimming")) {
            cir.setReturnValue((boolean) map.get("isVisuallySwimming"));
        }
    }

    @Inject(method = "getFallFlyingTicks", at = @At("TAIL"), cancellable = true)
    public void mixin$getFallFlyingTicks(CallbackInfoReturnable<Integer> cir) {
        var map = ((EntitySavingFields) this).map();
        if (map.containsKey("fallFlyingTicks")) {
            cir.setReturnValue((int) map.get("fallFlyingTicks"));
        }
    }
}
