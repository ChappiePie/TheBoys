package chappie.theboys.util;

import chappie.theboys.TheBoys;
import chappie.theboys.client.models.ModelCape;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ElytraItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import xyz.heroesunited.heroesunited.util.HUClientUtil;

public class TBClientUtil {

    public static void renderHeatvision(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, boolean isRightEye, float red, float green, float blue) {
        double distance = player.getPositionVec().add(0, player.getEyeHeight(), 0).distanceTo(Minecraft.getInstance().objectMouseOver.getHitVec());
        matrix.push();
        renderer.getEntityModel().bipedHead.translateRotate(matrix);
        matrix.scale(0.5F, 0.75F, 1);
        matrix.translate(isRightEye ? -0.15 : 0.15,-0.05,0);
        AxisAlignedBB box = new AxisAlignedBB(isRightEye ? -0.1F : 0.1F, -4F * 0.0625F, 0, 0, -4F * 0.0625F, -distance);
        HUClientUtil.renderGlowingLine(matrix, bufferIn, box, red, green, blue, 1f, packedLightIn);
        matrix.pop();
    }

    public static void renderCape(IEntityRenderer<? extends Entity, ? extends EntityModel<?>> renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, Entity entity, float partialTicks, ResourceLocation texture) {
        renderCape(renderer, matrix, bufferIn, packedLightIn, entity, partialTicks, texture, false);
    }

    public static void renderCape(IEntityRenderer<? extends Entity, ? extends EntityModel<?>> renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, Entity entity, float partialTicks, ResourceLocation texture, boolean haveNullRotation) {
        if (renderer != null && renderer.getEntityModel() instanceof BipedModel && entity instanceof LivingEntity) {

            if (((LivingEntity) entity).getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() instanceof ElytraItem || entity instanceof ClientPlayerEntity && ((PlayerEntity) entity).isWearing(PlayerModelPart.CAPE) && ((ClientPlayerEntity) entity).getLocationCape() != null) {
                return;
            }
            final ModelCape model = new ModelCape();
            matrix.push();
            ((BipedModel) renderer.getEntityModel()).bipedBody.translateRotate(matrix);
            matrix.translate(0, -0.04F, 0.05F);
            matrix.scale(0.9F, 0.9F, 0.9F);
            if ((((LivingEntity) entity).isElytraFlying() || haveNullRotation)) {
                model.cape.rotateAngleX = 0F;
                model.cape.rotateAngleY = 0F;
                model.cape.rotateAngleZ = 0F;
            } else if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                double d0 = MathHelper.lerp(partialTicks, player.prevChasingPosX, player.chasingPosX) - MathHelper.lerp(partialTicks, player.prevPosX, player.getPosX());
                double d1 = MathHelper.lerp(partialTicks, player.prevChasingPosY, player.chasingPosY) - MathHelper.lerp(partialTicks, player.prevPosY, player.getPosY());
                double d2 = MathHelper.lerp(partialTicks, player.prevChasingPosZ, player.chasingPosZ) - MathHelper.lerp(partialTicks, player.prevPosZ, player.getPosZ());
                float f = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset);
                double d3 = MathHelper.sin(f * ((float) Math.PI / 180F));
                double d4 = -MathHelper.cos(f * ((float) Math.PI / 180F));
                float f1 = (float) d1 * 10.0F;
                f1 = MathHelper.clamp(f1, -6.0F, 32.0F);
                float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
                f2 = MathHelper.clamp(f2, 0.0F, 150.0F);
                float f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;
                f3 = MathHelper.clamp(f3, -20.0F, 20.0F);
                if (f2 < 0.0F) {
                    f2 = 0.0F;
                }

                float f4 = MathHelper.lerp(partialTicks, player.prevCameraYaw, player.cameraYaw);
                f1 = f1 + MathHelper.sin(MathHelper.lerp(partialTicks, player.prevDistanceWalkedModified, player.distanceWalkedModified) * 6.0F) * 32.0F * f4;

                model.cape.rotateAngleX = (float) -Math.toRadians(6.0F + f2 / 2.0F + f1);
                model.cape.rotateAngleY = (float) Math.toRadians(180.0F - f3 / 2.0F);
                model.cape.rotateAngleZ = (float) Math.toRadians(f3 / 2.0F);
            }
            model.render(matrix, bufferIn.getBuffer(RenderType.getEntitySolid(texture)), packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
            matrix.pop();
        }
    }

    public static class TBRenderTypes extends RenderType {

        public TBRenderTypes(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
            super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
        }

        public static final RenderType TRAIL = makeType(TheBoys.MODID + ":trail", DefaultVertexFormats.POSITION_COLOR, 7, 256, true, true,
                State.getBuilder().texturing(TexturingState.ENTITY_GLINT_TEXTURING)
                        .transparency(RenderState.LIGHTNING_TRANSPARENCY)
                        .texture(RenderState.NO_TEXTURE)
                        .cull(RenderState.CULL_ENABLED)
                        .alpha(DEFAULT_ALPHA)
                        .lightmap(RenderState.LIGHTMAP_ENABLED)
                        .build(true));
    }
}
