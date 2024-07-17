package chappie.theboys.mixin.client;

import chappie.theboys.common.item.suit.SuitItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
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

    @Shadow public abstract BakedModel getModel(ItemStack pStack, @Nullable Level pLevel, @Nullable LivingEntity pEntity, int pSeed);

    @Shadow protected abstract void renderGuiItem(PoseStack p_275246_, ItemStack p_275195_, int p_275214_, int p_275658_, BakedModel p_275740_);

    @Shadow public abstract void render(ItemStack pItemStack, ItemDisplayContext pDisplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay, BakedModel pModel);

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderModelLists(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemStack;IILcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"))
    private void tryRenderGuiItem(ItemStack pStack, ItemDisplayContext pDisplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay, BakedModel pModel, CallbackInfo ci) {
        if (pStack.getItem() instanceof ArmorItem armorItem && pStack.getOrCreateTag().contains("Suit")) {
            CompoundTag tag = pStack.getOrCreateTag().getCompound("Suit");
            ItemStack stack = ItemStack.of(tag.getCompound("Tags"));
            if (stack.getItem() instanceof SuitItem item
                    && armorItem.getEquipmentSlot() == item.properties.getSlot()) {
                BakedModel pBakedModel = this.getModel(stack, this.minecraft.level, this.minecraft.player, 0);
                pPoseStack.pushPose();

                this.render(stack, ItemDisplayContext.HEAD, false, pPoseStack, pBuffer, pCombinedLight, pCombinedOverlay, pBakedModel);
                pPoseStack.popPose();
            }
        }

    }
}