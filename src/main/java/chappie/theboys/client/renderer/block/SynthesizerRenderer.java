package chappie.theboys.client.renderer.block;

import chappie.theboys.TheBoys;
import chappie.theboys.common.block.entity.SynthesizerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class SynthesizerRenderer extends GeoBlockRenderer<SynthesizerBlockEntity> {

    public SynthesizerRenderer() {
        super(new DefaultedBlockGeoModel<>(TheBoys.id("synthesizer")));
        this.withScale(0.85F);
    }

    @Override
    public void preRender(PoseStack poseStack, SynthesizerBlockEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int renderColor) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, renderColor);
        try {
            float f = animatable.workTimer.value(partialTick);
            float f1 = 360F * animatable.rollTimer.value(partialTick);
            model.getBone("bone20").orElseThrow().setRotZ((float) Math.toRadians(7.5F * Math.sin(this.animatable.tickCount / 2F) * f));
            model.getBone("plate").orElseThrow().setRotX((float) Math.toRadians(70F * this.animatable.openTimer.value(partialTick)));
            model.getBone("rotateit").orElseThrow().setRotY((float) Math.toRadians(f1));
        } catch (Throwable e) {
            e.fillInStackTrace();
        }

        for (int i = 3; i < 9; i++) {
            boolean b = animatable.getItem(i).isEmpty();
            model.getBone("bone_%s".formatted(i - 2)).ifPresent(bone -> bone.setHidden(b));
        }
    }

    @Override
    protected void rotateBlock(Direction facing, PoseStack poseStack) {
        super.rotateBlock(facing, poseStack);
        poseStack.translate(0, 0, 0.0625F * 0.5F);
    }
}
