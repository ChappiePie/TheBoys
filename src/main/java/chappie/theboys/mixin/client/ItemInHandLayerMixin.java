package chappie.theboys.mixin.client;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.render.IRenderStateEntity;
import chappie.theboys.common.capability.TheBoysCap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin<S extends ArmedEntityRenderState, M extends EntityModel<S> & ArmedModel> {

    @Inject(method = "submitArmWithItem(Lnet/minecraft/client/renderer/entity/state/ArmedEntityRenderState;Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"))
    public void renderOffHand(S renderState, ItemStackRenderState stackRenderState, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, CallbackInfo ci) {
        if (renderState instanceof IRenderStateEntity<?> state) {
            LivingEntity entity = state.modulus$entity();
            TheBoysCap theBoysCap = TheBoysCap.getCap(entity);
            float partialTicks = ClientUtil.getPartialTick();
            if (theBoysCap != null && entity instanceof Player pPlayer && pPlayer.getMainArm().getOpposite() == arm) {
                float f = theBoysCap.vialAnim.insertVial.value(partialTicks);
                poseStack.mulPose(Axis.YP.rotationDegrees((pPlayer.getMainArm() == HumanoidArm.RIGHT ? 30F : 60F) * f));
                poseStack.translate((pPlayer.getMainArm() == HumanoidArm.RIGHT ? 0.01F : 0.025) * f, 0, 0);
            }
        }
    }
}
