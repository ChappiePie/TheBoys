package chappie.theboys.mixin.client;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.render.IRenderStateEntity;
import chappie.theboys.common.ability.FlightAbility;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends HumanoidRenderState> {

    @Shadow
    @Final
    public ModelPart rightArm;

    @Shadow
    @Final
    public ModelPart leftArm;

    @Inject(method = "poseRightArm", at = @At("HEAD"))
    public void slowDownRightArm(T renderState, CallbackInfo ci) {
        if (renderState instanceof IRenderStateEntity state) {
            for (FlightAbility ability : CommonUtil.getAbilitiesByType(FlightAbility.class, state.modulus$entity())) {
                this.rightArm.xRot -= this.rightArm.xRot * ability.timer.value(ClientUtil.getPartialTick());
            }
        }
    }

    @Inject(method = "poseLeftArm", at = @At("HEAD"))
    public void slowDownLeftArm(T renderState, CallbackInfo ci) {
        if (renderState instanceof IRenderStateEntity state) {
            for (FlightAbility ability : CommonUtil.getAbilitiesByType(FlightAbility.class, state.modulus$entity())) {
                this.leftArm.xRot -= this.leftArm.xRot * ability.timer.value(ClientUtil.getPartialTick());
            }
        }
    }
}
