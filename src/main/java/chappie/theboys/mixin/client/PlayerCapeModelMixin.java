package chappie.theboys.mixin.client;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.render.IRenderStateEntity;
import chappie.theboys.common.ability.FlightAbility;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.PlayerCapeModel;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerCapeModel.class)
public class PlayerCapeModelMixin {

    @WrapOperation(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;)V",
            at = @At(value = "INVOKE", target = "Lorg/joml/Quaternionf;rotateX(F)Lorg/joml/Quaternionf;")
    )
    private Quaternionf rotateWithFlight(Quaternionf instance, float angle, Operation<Quaternionf> original, @Local(argsOnly = true) PlayerRenderState playerRenderState) {
       if (playerRenderState instanceof IRenderStateEntity<?> entity) {
           for (FlightAbility a : CommonUtil.listOfType(FlightAbility.class, CommonUtil.getAbilities(entity.modulus$entity()))) {
               return instance.rotateX(angle * (1.0F - a.sprintingTimer.value(ClientUtil.getPartialTick())));
           }
       }
        return original.call(instance, angle);
    }
}
