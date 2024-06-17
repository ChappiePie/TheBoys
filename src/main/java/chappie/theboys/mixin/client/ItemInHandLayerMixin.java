package chappie.theboys.mixin.client;

import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.item.TBItems;
import chappie.theboys.util.TBClientUtil;
import chappie.theboys.util.timers.SyringeVialAnim;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin {

    @Inject(method = "renderArmWithItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    public void renderOffHand(LivingEntity pLivingEntity, ItemStack pItemStack, ItemDisplayContext pDisplayContext, HumanoidArm pArm, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
        TheBoysCap theBoysCap = TheBoysCap.getCap(pLivingEntity);
        float partialTicks = Minecraft.getInstance().getPartialTick();
        if (theBoysCap != null && pLivingEntity instanceof Player pPlayer && pPlayer.getOffhandItem().equals(pItemStack)) {
            float f = theBoysCap.vialAnim.insertVial.value(partialTicks);
            pPoseStack.mulPose(Axis.YP.rotationDegrees((pPlayer.getMainArm() == HumanoidArm.RIGHT ? 30F : 60F) * f));
            pPoseStack.translate((pPlayer.getMainArm() == HumanoidArm.RIGHT ? 0.01F : 0.025) * f, 0, 0);
        }
    }
}
