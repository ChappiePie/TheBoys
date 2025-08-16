package chappie.theboys.mixin.client;

import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    // @TODO

    /*
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
    }*/
}