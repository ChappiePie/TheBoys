package chappie.theboys.mixin.client;

import chappie.modulus.client.model.SuitModel;
import chappie.modulus.util.ClientUtil;
import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.util.ClientSuitProperties;
import chappie.theboys.util.TBConfig;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, A extends HumanoidModel<T>> {

    @Unique
    private SuitModel<T> theBoys$model;

    @Shadow
    protected abstract void setPartVisibility(A pModel, EquipmentSlot pSlot);

    @Inject(method = "renderArmorPiece*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ArmorItem;Lnet/minecraft/client/model/HumanoidModel;ZFFFLjava/lang/String;)V"), remap = false)
    private void startRenderArmorPiece(PoseStack pPoseStack, MultiBufferSource pBuffer, T pLivingEntity, EquipmentSlot pSlot, int pPackedLight, A pModel, CallbackInfo ci) {
        if (theBoys$model == null)
            theBoys$model = new SuitModel<>(SuitModel.SUIT);
        ItemStack stack = pLivingEntity.getItemBySlot(pSlot);
        if (stack.getItem() instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == pSlot) {
            if (stack.getOrCreateTag().contains("Suit")) {
                CompoundTag tag = stack.getOrCreateTag().getCompound("Suit");
                if (ItemStack.of(tag.getCompound("Tags")).getItem() instanceof SuitItem item) {
                    Vector3f vec3f = item.getClientSuitProperties().armorScale(pLivingEntity, stack);
                    switch (pSlot) {
                        case HEAD -> {
                            ClientUtil.modified(pModel.head).setSize(vec3f);
                            ClientUtil.modified(pModel.hat).setSize(vec3f);
                        }
                        case CHEST -> {
                            ClientUtil.modified(pModel.body).setSize(vec3f);
                            ClientUtil.modified(pModel.rightArm).setSize(vec3f);
                            ClientUtil.modified(pModel.leftArm).setSize(vec3f);
                        }
                        case LEGS -> {
                            ClientUtil.modified(pModel.body).setSize(vec3f);
                            ClientUtil.modified(pModel.rightLeg).setSize(vec3f);
                            ClientUtil.modified(pModel.leftLeg).setSize(vec3f);
                        }
                        case FEET -> {
                            ClientUtil.modified(pModel.rightLeg).setSize(vec3f);
                            ClientUtil.modified(pModel.leftLeg).setSize(vec3f);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V", at = @At("TAIL"))
    private void stopRenderArmorPiece(PoseStack pPoseStack, MultiBufferSource pBuffer, T pLivingEntity, EquipmentSlot pSlot, int pPackedLight, A pModel, CallbackInfo ci, @Local(ordinal = 0) ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armoritem && armoritem.getEquipmentSlot() == pSlot) {
            if (stack.getOrCreateTag().contains("Suit")) {
                CompoundTag tag = stack.getOrCreateTag().getCompound("Suit");
                ItemStack suitStack = ItemStack.of(tag.getCompound("Tags"));
                if (suitStack.getItem() instanceof SuitItem item) {
                    ClientSuitProperties properties = item.getClientSuitProperties();
                    float alpha = TBConfig.COMMON.suitOpacity.get().floatValue();
                    pModel.copyPropertiesTo(this.theBoys$model);
                    this.setPartVisibility((A) this.theBoys$model, pSlot);
                    pPoseStack.pushPose();
                    ClientUtil.modifyAllParts(this.theBoys$model, (part, iPart) -> iPart.setSize(properties.suitScale(pSlot, pLivingEntity, stack)));
                    RenderType renderType = RenderType.entityTranslucent(properties.suitTexture(pSlot, pLivingEntity, stack, ""));
                    properties.renderSuitModel((SuitModel<LivingEntity>) this.theBoys$model, renderType, pPoseStack, pBuffer, pLivingEntity, pSlot, pPackedLight, stack, suitStack, pModel, alpha);
                    properties.render(pPoseStack, pBuffer, pLivingEntity, pSlot, pPackedLight, stack, suitStack, pModel, alpha);
                    pPoseStack.popPose();
                }
            }
        }
    }
}
