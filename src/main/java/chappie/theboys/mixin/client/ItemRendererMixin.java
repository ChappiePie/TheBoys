package chappie.theboys.mixin.client;

import chappie.theboys.common.item.suit.SuitItem;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow public abstract BakedModel getModel(ItemStack pStack, @Nullable Level pLevel, @Nullable LivingEntity pEntity, int pSeed);

    @Shadow protected abstract void renderGuiItem(PoseStack p_275246_, ItemStack p_275195_, int p_275214_, int p_275658_, BakedModel p_275740_);

    @Inject(method = "tryRenderGuiItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/client/resources/model/BakedModel;)V"))
    private void tryRenderGuiItem(PoseStack pPoseStack, LivingEntity pEntity, Level pLevel, ItemStack pStack, int pX, int pY, int pSeed, int p_275555_, CallbackInfo ci) {
        if (pStack.getItem() instanceof ArmorItem armorItem && pStack.getOrCreateTag().contains("Suit")) {
            CompoundTag tag = pStack.getOrCreateTag().getCompound("Suit");
            ItemStack stack = ItemStack.of(tag.getCompound("Tags"));
            if (stack.getItem() instanceof SuitItem item
                    && armorItem.getEquipmentSlot() == item.properties.getSlot()) {
                BakedModel bakedmodel = this.getModel(stack, pLevel, pEntity, pSeed);
                pPoseStack.pushPose();
                pPoseStack.translate(0.0F, 0.0F, (float)(50 + (bakedmodel.isGui3d() ? p_275555_ : 0)));
                pPoseStack.translate(3, 1, 0);
                RenderSystem.applyModelViewMatrix();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1, 1, 1, 0.75F);
                this.renderGuiItem(pPoseStack, stack, pX, pY, bakedmodel);
                RenderSystem.disableBlend();
                RenderSystem.setShaderColor(1, 1, 1, 1F);
                pPoseStack.popPose();
            }
        }

    }
}