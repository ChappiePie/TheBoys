package chappie.theboys.mixin;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.FishSwarmAbility;
import chappie.theboys.util.TBCommonUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSchoolingFish.class)
public class AbstractSchoolingFishMixin {

    @Inject(method = "pathToLeader()V", at = @At("HEAD"), cancellable = true)
    public void canUse(CallbackInfo ci) {
        AbstractSchoolingFish fish = (AbstractSchoolingFish) (Object) this;
        for (Player player : fish.level().getEntitiesOfClass(Player.class,
                CommonUtil.boxWithRange(fish.position(), FishSwarmAbility.DETECTION_RADIUS))) {
            for (FishSwarmAbility ability : CommonUtil.listOfType(FishSwarmAbility.class,
                    CommonUtil.getAbilities(player))) {
                if (ability.isEnabled() && fish.position().distanceTo(player.position()) >= ability.dataManager.get(TBCommonUtil.DISTANCE)) {
                    ci.cancel();
                    break;
                }
            }
        }
    }

    @Inject(method = "tick", at = @At("RETURN"), cancellable = true)
    public void onTick(CallbackInfo ci) {
        AbstractSchoolingFish fish = (AbstractSchoolingFish) (Object) this;
        for (Player player : fish.level().getEntitiesOfClass(Player.class,
                CommonUtil.boxWithRange(fish.position(), FishSwarmAbility.DETECTION_RADIUS))) {
            for (FishSwarmAbility a : CommonUtil.listOfType(FishSwarmAbility.class,
                    CommonUtil.getAbilities(player))) {
                if (!a.isEnabled() || fish.position().distanceTo(player.position()) >= a.dataManager.get(TBCommonUtil.DISTANCE)) {
                    continue;
                }
                RandomSource rand = fish.getRandom();
                double angle = rand.nextDouble() * 2 * Math.PI;
                double distance = rand.nextDouble() * 3;
                Vec3 swarmTarget = new Vec3(player.getX() + Math.cos(angle) * distance,
                        player.getY() - 0.5 - rand.nextDouble() * 2F,
                        player.getZ() + Math.sin(angle) * distance);

                Vec3 toTarget = swarmTarget.subtract(fish.position());

                Vec3 randomMovement = new Vec3(
                        (rand.nextDouble() - 0.5) * 0.02,
                        (rand.nextDouble() - 0.5) * 0.01,
                        (rand.nextDouble() - 0.5) * 0.02
                ).scale(0.5);
                fish.setDeltaMovement(fish.getDeltaMovement().add(
                        toTarget.normalize().scale(Math.min(toTarget.length() / 4.0, 1.0) / 2F)
                ).add(randomMovement));

                if (toTarget.length() < 6) {
                    fish.setYRot(smoothRotation(fish.getYRot(), player.yBodyRot, 10.0f));
                    fish.yHeadRot = fish.getYRot();
                    fish.yBodyRot = fish.getYRot();
                    fish.setXRot(smoothRotation(fish.getXRot(), player.getXRot(), 5.0f));
                } else {
                    if (toTarget.lengthSqr() >= 0.0001) {
                        float xRot = -((float) Math.toDegrees(Mth.atan2(toTarget.y,
                                Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z))));

                        fish.setYRot(smoothRotation(fish.getYRot(), (float) Math.toDegrees(Mth.atan2(toTarget.z, toTarget.x)) - 90.0f, 20.0f));
                        fish.yHeadRot = fish.getYRot();
                        fish.yBodyRot = fish.getYRot();
                        fish.setXRot(smoothRotation(fish.getXRot(), Mth.clamp(xRot, -85, 85), 10.0F));
                    }
                }
                ci.cancel();
            }
        }
    }

    /**
     * Плавное вращение с ограничением скорости
     */
    @Unique
    private float smoothRotation(float currentAngle, float targetAngle, float maxChangeRate) {
        float angleDifference = Mth.wrapDegrees(targetAngle - currentAngle);
        return Mth.wrapDegrees(currentAngle + Mth.clamp(angleDifference, -maxChangeRate, maxChangeRate));
    }
}
