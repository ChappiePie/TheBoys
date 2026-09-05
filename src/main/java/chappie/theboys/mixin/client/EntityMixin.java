package chappie.theboys.mixin.client;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.FocusOnGoalAbility;
import chappie.theboys.common.ability.ParkourAbility;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Entity.class, priority = 900)
public abstract class EntityMixin {

    @Shadow
    public float xRotO;
    @Shadow
    public float yRotO;

    @Shadow
    private @Nullable Entity vehicle;

    @Shadow
    public abstract void setYRot(float yRot);

    @Shadow
    public abstract void setXRot(float xRot);

    @Inject(method = "turn(DD)V", at = @At("HEAD"), cancellable = true)
    public void mixin$turn(double yRot, double xRot, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;

        // ParkourAbility: override rotation while hanging on a ledge
        for (ParkourAbility ability : CommonUtil.listOfType(ParkourAbility.class, CommonUtil.getAbilities(entity))) {
            if (ability.ledgeHandler.isHanging() && entity instanceof Player player) {
                ability.ledgeHandler.applyTurn(player, yRot, xRot);
                ci.cancel();
                return;
            }
        }

        if (this.theBoys$focusOnGoal()) {
            ci.cancel();
        }
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void mixin$tick(CallbackInfo ci) {
        this.theBoys$focusOnGoal();
    }

    @Unique
    private boolean theBoys$focusOnGoal() {
        Entity entity = (Entity) (Object) this;
        for (FocusOnGoalAbility ability : CommonUtil.listOfType(FocusOnGoalAbility.class, CommonUtil.getAbilities(entity))) {
            if (ability.dataManager.get(FocusOnGoalAbility.TARGET_ID).equals(ability.entity.getId())) {
                continue;
            }
            Entity targetEntity = entity.level().getEntity(ability.dataManager.get(FocusOnGoalAbility.TARGET_ID));
            if (targetEntity == null) {
                continue;
            }
            Vec3 target = targetEntity.getEyePosition(ClientUtil.getPartialTick());
            Vec3 eyePos = entity.getEyePosition(ClientUtil.getPartialTick());
            double distance = target.distanceTo(eyePos);

            if ((!ability.hasSpeedAbility() || distance > 2) && distance < 40) {
                Vec3 vec3 = target.subtract(eyePos);
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
                return true;
            }
        }
        return false;
    }
}
