package chappie.theboys.mixin.client;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.FlightAbility;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CapeLayer.class)
public class CapeLayerMixin {

    @WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/math/Axis;rotationDegrees(F)Lorg/joml/Quaternionf;", ordinal = 0)
    )
    private Quaternionf changeObject(Axis instance, float f, Operation<Quaternionf> original, @Local(argsOnly = true) AbstractClientPlayer player, @Local(argsOnly = true, ordinal = 2) float partialTicks) {
        for (FlightAbility a : CommonUtil.listOfType(FlightAbility.class, CommonUtil.getAbilities(player))) {
            return instance.rotationDegrees(f * (1.0F - a.sprintingTimer.value(partialTicks)));
        }
        return original.call(instance, f);
    }
}
