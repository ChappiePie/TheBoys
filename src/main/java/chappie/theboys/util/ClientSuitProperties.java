package chappie.theboys.util;

import chappie.modulus.util.ClientUtil;
import chappie.theboys.TheBoys;
import chappie.theboys.common.item.suit.SuitItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
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

    public Identifier suitTexture(EquipmentSlot slot, ItemStack armorStack, String type) {
        return TheBoys.id("textures/suits/%s/layer_%s.png".formatted(this.type(), slot == EquipmentSlot.LEGS ? 1 : 0));
    }

    public void render(PoseStack pPoseStack, SubmitNodeCollector submitNodeCollector, HumanoidRenderState renderState, EquipmentSlot pSlot, int pPackedLight, ItemStack armorStack, ItemStack suitStack, HumanoidModel<?> model, float alpha) {
    }

    public void renderSuitModel(HumanoidModel<HumanoidRenderState> suitModel, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, HumanoidRenderState renderState, EquipmentSlot slot, int pPackedLight, ItemStack armorStack, ItemStack suitStack, HumanoidModel<?> pModel, float alpha) {
        submitNodeCollector.submitModel(
                suitModel,
                renderState,
                poseStack,
                RenderTypes.entityTranslucent(this.suitTexture(slot, armorStack, "")),
                pPackedLight,
                OverlayTexture.NO_OVERLAY,
                ARGB.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F),
                null,
                renderState.outlineColor,
                null
        );
    }

    public void setupSuitScale(HumanoidModel<?> model, @Nullable Entity entity, EquipmentSlot slot, ItemStack armorItem) {
        ClientUtil.modifyAllParts(model, (part, iPart) -> {
            Vector3f vec = this.suitScale(slot, armorItem);
            iPart.modulus$setSize(vec);
            if (ClientUtil.smallArms(entity)) {
                ModelPart rightSleeve = model.rightArm.hasChild("right_sleeve") ? model.rightArm.getChild("right_sleeve") : null;
                ModelPart leftSleeve = model.leftArm.hasChild("left_sleeve") ? model.leftArm.getChild("left_sleeve") : null;
                if (part == model.rightArm || part == rightSleeve) {
                    iPart.modulus$setSizeAndPos(vec.add(-0.5F, 0, 0), new Vector3f(0.5F, 0, 0));
                } else if (part == model.leftArm || part == leftSleeve) {
                    iPart.modulus$setSizeAndPos(vec.add(-0.5F, 0, 0), new Vector3f(-0.5F, 0, 0));
                }
            }
        });
    }
}
