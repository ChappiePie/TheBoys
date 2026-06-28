package chappie.theboys.mixin.client;

import chappie.modulus.util.render.IRenderStateEntity;
import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.util.ClientSuitProperties;
import chappie.theboys.util.TBClientUtil;
import chappie.theboys.util.TBConfig;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
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
    private static final EquipmentSlot[] theBoys$ARMOR_SLOTS = Arrays.stream(EquipmentSlot.values())
            .filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
            .toArray(EquipmentSlot[]::new);
    @Unique
    private ArmorModelSet<HumanoidModel<HumanoidRenderState>> theBoys$suitModelSet;

    @Shadow protected abstract A getArmorModel(S renderState, EquipmentSlot slot);

    @WrapWithCondition(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V"))
    private boolean getModifiedArmorModel(HumanoidArmorLayer<S, M, A> instance, PoseStack poseStack, SubmitNodeCollector nodeCollector, ItemStack item, EquipmentSlot slot, int packedLight, S renderState) {
        this.theBoys$ensureSuitModelSet();
        TBClientUtil.modifySizeOfArmor(item, slot, this.getArmorModel(renderState, slot), renderState);
        return true;
    }

    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At("TAIL"))
    private void stopRenderArmorPiece(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, S renderState, float f, float g, CallbackInfo ci) {
        this.theBoys$ensureSuitModelSet();
        for (EquipmentSlot slot : theBoys$ARMOR_SLOTS) {
            ItemStack armorItem = this.theBoys$getArmorItem(renderState, slot);
            if (armorItem.isEmpty()) {
                continue;
            }
            A model = this.getArmorModel(renderState, slot);
            this.theBoys$renderSuitLayer(poseStack, submitNodeCollector, packedLight, renderState, slot, armorItem, model);
        }
    }

    @Unique
    private void theBoys$ensureSuitModelSet() {
        if (this.theBoys$suitModelSet == null) {
            this.theBoys$suitModelSet = ArmorModelSet.bake(TBClientUtil.SUIT, Minecraft.getInstance().getEntityModels(), HumanoidModel::new);
        }
    }

    @Unique
    private ItemStack theBoys$getArmorItem(S renderState, EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> renderState.headEquipment;
            case CHEST -> renderState.chestEquipment;
            case LEGS -> renderState.legsEquipment;
            case FEET -> renderState.feetEquipment;
            default -> ItemStack.EMPTY;
        };
    }

    @Unique
    private void theBoys$renderSuitLayer(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, S renderState, EquipmentSlot slot, ItemStack armorItem, A model) {
        Equippable equippable = armorItem.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.slot() != slot) {
            return;
        }

        ItemStack suitItem = armorItem.getOrDefault(TBDataComponents.SUIT, ItemStack.EMPTY);
        if (!(suitItem.getItem() instanceof SuitItem suit)) {
            return;
        }

        ClientSuitProperties properties = suit.getClientSuitProperties();
        float alpha = TBConfig.COMMON.suitOpacity.get().floatValue();
        poseStack.pushPose();
        Entity entity = renderState instanceof IRenderStateEntity e ? e.modulus$entity() : null;
        HumanoidModel<HumanoidRenderState> suitModel = this.theBoys$suitModelSet.get(slot);
        properties.setupSuitScale(suitModel, entity, slot, armorItem);
        properties.renderSuitModel(suitModel, poseStack, submitNodeCollector, renderState, slot, packedLight, armorItem, suitItem, model, alpha);
        properties.render(poseStack, submitNodeCollector, renderState, slot, packedLight, armorItem, suitItem, model, alpha);
        poseStack.popPose();
    }
}
