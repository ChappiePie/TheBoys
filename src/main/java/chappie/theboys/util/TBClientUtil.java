package chappie.theboys.util;

import chappie.theboys.TheBoys;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.AxisAlignedBB;
import xyz.heroesunited.heroesunited.util.HUClientUtil;

import java.awt.*;

public class TBClientUtil {

    public static void renderHeatvision(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, boolean isRightEye, Color color) {
        double distance = player.getPositionVec().add(0, player.getEyeHeight(), 0).distanceTo(Minecraft.getInstance().objectMouseOver.getHitVec());
        matrix.push();
        renderer.getEntityModel().bipedHead.translateRotate(matrix);
        matrix.scale(0.5F, 0.75F, 1);
        matrix.translate(isRightEye ? -0.15 : 0.15,-0.05,0);
        AxisAlignedBB box = new AxisAlignedBB(isRightEye ? -0.1F : 0.1F, -4F * 0.0625F, 0, 0, -4F * 0.0625F, -distance);
        HUClientUtil.renderFilledBox(matrix, bufferIn.getBuffer(HUClientUtil.HURenderTypes.LASER), box.grow(0.0312D), 1F, 1F, 1F, 1f, packedLightIn);
        HUClientUtil.renderFilledBox(matrix, bufferIn.getBuffer(HUClientUtil.HURenderTypes.LASER), box.grow(0.0625D), -color.getRed(), -color.getGreen(), -color.getBlue(), 0.5F * 1f, packedLightIn);
        matrix.pop();
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
