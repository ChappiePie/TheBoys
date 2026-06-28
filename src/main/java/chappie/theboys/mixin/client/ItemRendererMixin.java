package chappie.theboys.mixin.client;

import chappie.theboys.common.item.TBItems;
import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.util.TBClientUtil;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private ItemModelShaper itemModelShaper;

    @Shadow
    public abstract BakedModel getModel(ItemStack pStack, @Nullable Level pLevel, @Nullable LivingEntity pEntity, int pSeed);

    @Shadow
    public abstract void render(ItemStack pItemStack, ItemDisplayContext pDisplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay, BakedModel pModel);

    @Inject(method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderModelLists(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemStack;IILcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"))
    private void tryRenderGuiItem(ItemStack pStack, ItemDisplayContext pDisplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay, BakedModel pModel, CallbackInfo ci) {
        if (pStack.getItem() instanceof ArmorItem armorItem) {
            ItemStack stack = pStack.has(TBDataComponents.SUIT) ? pStack.get(TBDataComponents.SUIT).toStack() : ItemStack.EMPTY;
            if (stack.getItem() instanceof SuitItem item
                    && armorItem.getEquipmentSlot() == item.properties.getSlot()) {
                BakedModel pBakedModel = this.getModel(stack, this.minecraft.level, this.minecraft.player, 0);
                pPoseStack.pushPose();

                this.render(stack, ItemDisplayContext.HEAD, false, pPoseStack, pBuffer, pCombinedLight, pCombinedOverlay, pBakedModel);
                pPoseStack.popPose();
            }
        }
    }

    @Inject(method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V", at = @At("HEAD"))
    public void theBoys$model(ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model, CallbackInfo ci, @Local(argsOnly = true) LocalRef<BakedModel> bakedModel) {
        if (!itemStack.isEmpty()) {
            boolean bl = displayContext == ItemDisplayContext.GUI || displayContext == ItemDisplayContext.GROUND || displayContext == ItemDisplayContext.FIXED;
            BakedModel bModel = null;
            if (!bl) {
                if (itemStack.is(TBItems.SYRINGE)) {
                    bModel = this.itemModelShaper.getModelManager().getModel(TBClientUtil.SYRINGE_3D_MODEL);
                } else if (itemStack.is(TBItems.VIAL)) {
                    bModel = this.itemModelShaper.getModelManager().getModel(TBClientUtil.VIAL_3D_MODEL);
                }
            }
            if (bModel != null) {
                bakedModel.set(bModel.getOverrides().resolve(bModel, itemStack, Minecraft.getInstance().level, null, -1));
            }
        }
    }
}
