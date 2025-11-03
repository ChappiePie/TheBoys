package chappie.theboys.util;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.theboys.TheBoys;
import chappie.theboys.common.item.suit.SuitItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
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

    public Vector3f entityWearScale(EquipmentSlot slot, ItemStack armorStack) {
        return new Vector3f(slot == EquipmentSlot.HEAD ? -0.499F : -0.249F);
    }

    public Vector3f armorScale(EquipmentSlot slot, ItemStack armorItem) {
        return this.suitItem.properties.armorScale.apply(armorItem);
    }

    public Vector3f suitScale(EquipmentSlot slot, ItemStack armorStack) {
        return new Vector3f(slot == EquipmentSlot.LEGS ? 0.1F : 0.25F);
    }

    public ResourceLocation suitTexture(EquipmentSlot slot, ItemStack armorStack, String type) {
        return TheBoys.id("textures/suits/%s/layer_%s.png".formatted(this.type(), slot == EquipmentSlot.LEGS ? 1 : 0));
    }

    public void render(PoseStack pPoseStack, SubmitNodeCollector submitNodeCollector, HumanoidRenderState renderState, EquipmentSlot pSlot, int pPackedLight, ItemStack armorStack, ItemStack suitStack, HumanoidModel<?> model, float alpha) {
    }

    public void renderSuitModel(PlayerModel suitModel, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, HumanoidRenderState renderState, EquipmentSlot slot, int pPackedLight, ItemStack armorStack, ItemStack suitStack, HumanoidModel<?> pModel, float alpha) {
        RenderType type = RenderType.entityTranslucent(this.suitTexture(slot, armorStack, ""));
        submitNodeCollector.submitCustomGeometry(poseStack, type, (pose, vertexConsumer) -> {
            suitModel.renderToBuffer(poseStack, vertexConsumer, pPackedLight, OverlayTexture.NO_OVERLAY, ARGB.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F));
        });
    }

    public <S extends HumanoidRenderState> void setupSuitScale(PlayerModel model, Entity entity, EquipmentSlot slot, ItemStack armorItem) {
        ClientUtil.modifyAllParts(model, (part, iPart) -> {
            Vector3f vec = this.suitScale(slot, armorItem);
            iPart.modulus$setSize(vec);
            if (CommonUtil.smallArms(entity)) {
                if (part == model.rightArm || part == model.rightSleeve) {
                    iPart.modulus$setSizeAndPos(vec.add(-0.5F, 0, 0), new Vector3f(0.5F, 0, 0));
                } else if (part == model.leftArm || part == model.leftSleeve) {
                    iPart.modulus$setSizeAndPos(vec.add(-0.5F, 0, 0), new Vector3f(-0.5F, 0, 0));
                }
            }
        });
        model.setAllVisible(false);
        switch (slot) {
            case HEAD:
                model.head.visible = true;
                model.hat.visible = true;
                break;
            case CHEST:
                model.body.visible = true;
                model.jacket.visible = true;
                model.rightArm.visible = true;
                model.rightSleeve.visible = true;
                model.leftArm.visible = true;
                model.leftSleeve.visible = true;
                break;
            case LEGS:
                model.body.visible = true;
                model.jacket.visible = true;
                model.rightLeg.visible = true;
                model.rightPants.visible = true;
                model.leftLeg.visible = true;
                model.leftPants.visible = true;
                break;
            case FEET:
                model.rightLeg.visible = true;
                model.rightPants.visible = true;
                model.leftLeg.visible = true;
                model.leftPants.visible = true;
        }
    }
}
