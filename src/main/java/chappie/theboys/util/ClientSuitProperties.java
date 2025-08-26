package chappie.theboys.util;

import chappie.theboys.TheBoys;
import chappie.theboys.common.item.suit.SuitItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public class ClientSuitProperties {

    public final SuitItem suitItem;

    public ClientSuitProperties(SuitItem suitItem) {
        this.suitItem = suitItem;
    }

    protected String type() {
        return this.suitItem.properties.type;
    }

    public Vector3f entityWearScale(EquipmentSlot slot, LivingEntityRenderState entity, ItemStack armorStack) {
        return new Vector3f(slot == EquipmentSlot.HEAD ? -0.499F : -0.249F);
    }

    public Vector3f armorScale(HumanoidRenderState renderState, ItemStack armorItem) {
        return this.suitItem.properties.armorScale.apply(armorItem);
    }

    public Vector3f suitScale(EquipmentSlot slot, HumanoidRenderState renderState, ItemStack armorStack) {
        return new Vector3f(slot == EquipmentSlot.LEGS ? 0.3F : 0.45F);
    }

    public ResourceLocation suitTexture(EquipmentSlot slot, HumanoidRenderState renderState, ItemStack armorStack, String type) {
        return ResourceLocation.fromNamespaceAndPath(TheBoys.MODID, "textures/suits/%s/layer_%s.png".formatted(this.type(), slot == EquipmentSlot.LEGS ? 1 : 0));
    }

    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, HumanoidRenderState renderState, EquipmentSlot pSlot, int pPackedLight, ItemStack armorStack, ItemStack suitStack, HumanoidModel<?> model, float alpha) {
    }

    public void renderSuitModel(PlayerModel suitModel, RenderType renderType, PoseStack pPoseStack, MultiBufferSource pBuffer, HumanoidRenderState renderState, EquipmentSlot pSlot, int pPackedLight, ItemStack stack, ItemStack suitStack, HumanoidModel<?> pModel, float alpha) {
        suitModel.renderToBuffer(pPoseStack, pBuffer.getBuffer(renderType), pPackedLight, OverlayTexture.NO_OVERLAY, ARGB.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F));
    }
}
