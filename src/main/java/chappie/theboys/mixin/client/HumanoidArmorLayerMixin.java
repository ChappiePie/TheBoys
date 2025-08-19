package chappie.theboys.mixin.client;

import chappie.modulus.util.ClientUtil;
import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.util.ClientSuitProperties;
import chappie.theboys.util.TBConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> {

    @Unique
    private PlayerModel theBoys$model;

    @Shadow
    protected abstract void setPartVisibility(M pModel, EquipmentSlot pSlot);

    @Shadow protected abstract A getArmorModel(S renderState, EquipmentSlot slot);

    @Inject(method = "getArmorModel(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/client/model/HumanoidModel;", at = @At("RETURN"))
    private void getModifiedArmorModel(S renderState, EquipmentSlot slot, CallbackInfoReturnable<M> cir) {
        if (theBoys$model == null)
            theBoys$model = new PlayerModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER), false);

        ItemStack armorItem = switch (slot) {
            case HEAD -> renderState.headEquipment;
            case CHEST -> renderState.chestEquipment;
            case LEGS -> renderState.legsEquipment;
            case FEET -> renderState.feetEquipment;
            default -> ItemStack.EMPTY;
        };
        M pModel = cir.getReturnValue();

        Equippable equippable = armorItem.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.slot() == slot) {
            ItemStack suitItem = armorItem.getOrDefault(TBDataComponents.SUIT, ItemStack.EMPTY);
            if (!suitItem.isEmpty() && suitItem.getItem() instanceof SuitItem item) {
                Vector3f vec3f = item.getClientSuitProperties().armorScale(renderState, armorItem);
                switch (slot) {
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
                    model.copyPropertiesTo((HumanoidModel<S>) this.theBoys$model);
                    this.setPartVisibility((M) this.theBoys$model, slot);
                    poseStack.pushPose();
                    ClientUtil.modifyAllParts(this.theBoys$model, (part, iPart) -> iPart.setSize(properties.suitScale(slot, renderState, armorItem)));
                    RenderType renderType = RenderType.entityTranslucent(properties.suitTexture(slot, renderState, armorItem, ""));
                    properties.renderSuitModel(this.theBoys$model, renderType, poseStack, multiBufferSource, renderState, slot, packedLight, armorItem, suitItem, model, alpha);
                    properties.render(poseStack, multiBufferSource, renderState, slot, packedLight, armorItem, suitItem, model, alpha);
                    poseStack.popPose();
                }
            }
        }
    }
}
