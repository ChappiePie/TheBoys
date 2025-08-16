package chappie.theboys.util;

import chappie.theboys.TheBoys;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.item.TBItems;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class TBClientUtil {

    public static final ModelResourceLocation SYRINGE_MODEL = new ModelResourceLocation(TheBoys.id("syringe"), "inventory");
    public static final ModelResourceLocation SYRINGE_3D_MODEL = new ModelResourceLocation(TheBoys.id("syringe_3d"), "inventory");

    public static final ModelResourceLocation VIAL_MODEL = new ModelResourceLocation(TheBoys.id("vial"), "inventory");
    public static final ModelResourceLocation VIAL_3D_MODEL = new ModelResourceLocation(TheBoys.id("vial_3d"), "inventory");
    public static final ResourceLocation GLOW_EYES_OVERLAY = TheBoys.id("textures/gui/glow_eyes_overlay.png");

    public static void setupArms(PlayerModel model, HumanoidArm side, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, Player pPlayer, ModelPart pRendererArm, ModelPart pRendererArmwear, float partialTicks) {
        TheBoysCap cap = TheBoysCap.getCap(pPlayer);
        if (cap == null) return;
        float timeline = cap.vialAnim.timeline.value(partialTicks);
        float t = Math.min(timeline, 0.5F) * 2F;

        boolean flag1 = side == HumanoidArm.RIGHT;
        int i = flag1 ? 1 : -1;
        ItemStack stack = pPlayer.getMainArm() == side ? pPlayer.getMainHandItem() : pPlayer.getOffhandItem();

        boolean vial = stack.getItem() == TBItems.VIAL || pPlayer.getMainArm() != side && timeline > 0;
        if (stack.getItem() == TBItems.SYRINGE || vial) {
            pRendererArm.xRot = (float) Math.toRadians(-12.5F);
            pRendererArm.yRot = (float) Math.toRadians(50F * i);
            pRendererArm.zRot = (float) Math.toRadians(-30.5F * i);
            pPoseStack.translate(-0.25 * i, 0.1, -0.1);


            float t1 = Mth.sin(pPlayer.tickCount + partialTicks) * cap.vialAnim.rollVial.value(partialTicks);
            float t2 = cap.vialAnim.insertVial.value(partialTicks) * 0.2F;
            if (vial) {
                t *= -i;
                pRendererArm.xRot -= (float) (Math.toRadians(30F) * t) * -i;
                pRendererArm.yRot += (float) (Math.toRadians(75.5F + t1) * t);
                pRendererArm.zRot -= (float) (Math.toRadians(22.5F) * t);
                pRendererArm.x += 0.6F * t;
                pRendererArm.y -= (1.2F + t2) * t * -i;
                pRendererArm.z -= (5.2F + t2) * t * -i;
                t /= -i;
            } else {
                pRendererArm.yRot -= (float) (Math.toRadians(70F) * t * i);
                pRendererArm.zRot += (float) (Math.toRadians(45F) * t * i);
            }

            if (!stack.isEmpty()) {
                pPoseStack.pushPose();
                pRendererArm.translateAndRotate(pPoseStack);
                pPoseStack.mulPose(Axis.XN.rotationDegrees(90F + (vial ? -20 : 0)));
                pPoseStack.mulPose(Axis.YN.rotationDegrees(180F + 11.25F * i));
                pPoseStack.translate(-0.15 * i, 0, -0.4);
                if (vial) {
                    float f = 1.0F - 0.4F * t;
                    pPoseStack.scale(f, f, f);
                    pPoseStack.translate(0, 0.155 * t + t1 * 0.01F, 0);

                    pPoseStack.mulPose(Axis.YN.rotationDegrees(t1 * 20));
                }
                Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().renderItem(pPlayer, stack, flag1 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !flag1, pPoseStack, pBuffer, pCombinedLight);
                pPoseStack.popPose();
            }
        }
        if (pRendererArmwear != null) {
            pRendererArmwear.copyFrom(pRendererArm);
        }
    }

    public static class RenderTypes extends RenderType {

        public RenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
            super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
        }

        public static RenderType entityInvisibility(ResourceLocation pLocation) {
            RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER).setTextureState(new RenderStateShard.TextureStateShard(pLocation, TriState.FALSE, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setOutputState(ITEM_ENTITY_TARGET).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE).createCompositeState(true);
            return create("entity_invisibility", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, rendertype$compositestate);
        }
    }
}
