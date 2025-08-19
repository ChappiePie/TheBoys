package chappie.theboys.client.renderer;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.render.IRenderStateEntity;
import chappie.theboys.TheBoys;
import chappie.theboys.client.model.CapeModel;
import chappie.theboys.common.ability.FlightAbility;
import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.util.ClientSuitProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class ClientHeroWithCapeProperties extends ClientSuitProperties {
    public final CapeModel model = new CapeModel();

    public ClientHeroWithCapeProperties(SuitItem suitItem) {
        super(suitItem);
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, HumanoidRenderState renderState, EquipmentSlot pSlot, int pPackedLight, ItemStack armorStack, ItemStack suitStack, HumanoidModel<?> model, float alpha) {
        super.render(pPoseStack, pBuffer, renderState, pSlot, pPackedLight, armorStack, suitStack, model, alpha);
        if (renderState instanceof IRenderStateEntity<?> state && state.modulus$entity() instanceof AbstractClientPlayer player && pSlot == EquipmentSlot.CHEST) {
            this.model.setupAnim(renderState);

            ModelPart cape = this.model.root().getChild("cape");
            pPoseStack.pushPose();
            if (renderState.isCrouching) {
                pPoseStack.translate(0, 0.15, 0.025);
                pPoseStack.mulPose(Axis.XP.rotationDegrees(22.5F));
                cape.xRot -= (float) Math.toRadians(6.125F);
            } else {
                pPoseStack.translate(0, -0.02, 0.025);
            }
            for (FlightAbility a : CommonUtil.listOfType(FlightAbility.class, CommonUtil.getAbilities(player))) {
                float t = a.sprintingTimer.value(ClientUtil.getPartialTick());
                cape.xRot -= (float) ((cape.xRot - Math.toRadians(170)) * t);
            }
            pPoseStack.scale(0.85F, 0.85F, 0.85F);
            this.model.renderToBuffer(pPoseStack, pBuffer.getBuffer(this.renderType()), pPackedLight, OverlayTexture.NO_OVERLAY, ARGB.color((int) (alpha * 255), 255, 255, 255));
            pPoseStack.popPose();
        }
    }

    public RenderType renderType() {
        return RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath(TheBoys.MODID, "textures/suits/%s/cape.png".formatted(this.type())));
    }
}
