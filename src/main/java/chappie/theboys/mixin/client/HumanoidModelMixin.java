package chappie.theboys.mixin.client;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.FlightAbility;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends LivingEntity> {

    @Shadow
    @Final
    public ModelPart rightArm;

    @Shadow
    @Final
    public ModelPart leftArm;

    @Inject(method = "poseRightArm", at = @At("HEAD"))
    public void slowDownRightArm(T pLivingEntity, CallbackInfo ci) {
        for (FlightAbility ability : CommonUtil.listOfType(FlightAbility.class, CommonUtil.getAbilities(pLivingEntity))) {
            this.rightArm.xRot -= this.rightArm.xRot * ability.timer.value(ClientUtil.getPartialTick());
        }
    }

    @Inject(method = "poseLeftArm", at = @At("HEAD"))
    public void slowDownLeftArm(T pLivingEntity, CallbackInfo ci) {
        for (FlightAbility ability : CommonUtil.listOfType(FlightAbility.class, CommonUtil.getAbilities(pLivingEntity))) {
            this.leftArm.xRot -= this.leftArm.xRot * ability.timer.value(ClientUtil.getPartialTick());

        }
    }
}
