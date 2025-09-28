package chappie.theboys.mixin.client;

import chappie.modulus.util.render.IRenderStateEntity;
import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.util.ClientSuitProperties;
import chappie.theboys.util.TBClientUtil;
import chappie.theboys.util.TBConfig;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> {

    @Unique
    private PlayerModel theBoys$model;

    @Shadow protected abstract A getArmorModel(S renderState, EquipmentSlot slot);

    @WrapWithCondition(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V"))
    private boolean getModifiedArmorModel(HumanoidArmorLayer instance, PoseStack poseStack, MultiBufferSource bufferSource, ItemStack armorItem, EquipmentSlot slot, int packedLight, A model, @Local(argsOnly = true) S humanoidRenderState) {
        if (this.theBoys$model == null)
            this.theBoys$model = new PlayerModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER), false);

        TBClientUtil.modifySizeOfArmor(armorItem, slot, model, humanoidRenderState);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At("TAIL"))
    private void stopRenderArmorPiece(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, S renderState, float f, float g, CallbackInfo ci) {
        for (EquipmentSlot slot : Arrays.stream(EquipmentSlot.values()).filter(p -> p.getType() == EquipmentSlot.Type.HUMANOID_ARMOR).toList()) {
            ItemStack armorItem = switch (slot) {
                case HEAD -> renderState.headEquipment;
                case CHEST -> renderState.chestEquipment;
                case LEGS -> renderState.legsEquipment;
                case FEET -> renderState.feetEquipment;
                default -> ItemStack.EMPTY;
            };
            A model = this.getArmorModel(renderState, slot);
            Equippable equippable = armorItem.get(DataComponents.EQUIPPABLE);
            if (equippable != null && equippable.slot() == slot) {
                ItemStack suitItem = armorItem.getOrDefault(TBDataComponents.SUIT, ItemStack.EMPTY);
                if (!suitItem.isEmpty() && suitItem.getItem() instanceof SuitItem item) {
                    ClientSuitProperties properties = item.getClientSuitProperties();
                    float alpha = TBConfig.COMMON.suitOpacity.get().floatValue();
                    poseStack.pushPose();
                    model.copyPropertiesTo((HumanoidModel<S>) this.theBoys$model);
                    if (renderState instanceof IRenderStateEntity<?> e) {
                        properties.setupSuitScale(this.theBoys$model, e.modulus$entity(), slot, armorItem);
                    }
                    properties.renderSuitModel(this.theBoys$model, poseStack, multiBufferSource, renderState, slot, packedLight, armorItem, suitItem, model, alpha);
                    properties.render(poseStack, multiBufferSource, renderState, slot, packedLight, armorItem, suitItem, model, alpha);
                    poseStack.popPose();
                }
            }
        }
    }
}
