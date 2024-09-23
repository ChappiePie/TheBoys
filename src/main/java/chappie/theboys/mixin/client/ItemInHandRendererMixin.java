package chappie.theboys.mixin.client;

import chappie.theboys.client.ClientEvents;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.item.TBItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow
    protected abstract void renderPlayerArm(PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, float pEquippedProgress, float pSwingProgress, HumanoidArm pSide);

    @Inject(method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), cancellable = true)
    public void renderOffHand(AbstractClientPlayer pPlayer, float pPartialTicks, float pPitch, InteractionHand pHand, float pSwingProgress, ItemStack pStack, float pEquippedProgress, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, CallbackInfo ci) {
        if (ClientEvents.renderHand(pHand, pPartialTicks, pMatrixStack, pBuffer, pCombinedLight)) return;

        if (!pPlayer.isScoping() && !pPlayer.isInvisible()) {
            TheBoysCap theBoysCap = TheBoysCap.getCap(pPlayer);
            if (!pStack.isEmpty() && (pStack.getItem() == TBItems.SYRINGE || pStack.getItem() == TBItems.VIAL) || theBoysCap.vialAnim.hideOffHand(pPlayer, theBoysCap, pPartialTicks, pHand)) {
                pMatrixStack.pushPose();
                this.renderPlayerArm(pMatrixStack, pBuffer, pCombinedLight, pEquippedProgress, pSwingProgress, pHand == InteractionHand.MAIN_HAND ? pPlayer.getMainArm() : pPlayer.getMainArm().getOpposite());
                pMatrixStack.popPose();
                ci.cancel();
            }
        }
    }
}
