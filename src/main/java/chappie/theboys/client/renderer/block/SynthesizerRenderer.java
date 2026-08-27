package chappie.theboys.client.renderer.block;

import chappie.theboys.TheBoys;
import chappie.theboys.common.block.entity.SynthesizerBlockEntity;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

import java.util.HashMap;
import java.util.Map;

public class SynthesizerRenderer extends GeoBlockRenderer<SynthesizerBlockEntity, SynthesizerRenderer.SynthesizerRenderState> {

    public SynthesizerRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new DefaultedBlockGeoModel<>(TheBoys.id("synthesizer")));
        this.withScale(0.85F);
    }

    @Override
    public SynthesizerRenderState createRenderState() {
        return new SynthesizerRenderState();
    }

    @Override
    public void captureDefaultRenderState(SynthesizerBlockEntity animatable, Void relatedObject, SynthesizerRenderState renderState, float partialTick) {
        super.captureDefaultRenderState(animatable, relatedObject, renderState, partialTick);
        renderState.blockEntity = animatable;
        renderState.partialTick = partialTick;
    }

    @Override
    public void preRenderPass(RenderPassInfo<SynthesizerRenderState> renderPassInfo, SubmitNodeCollector submitNodeCollector) {
        super.preRenderPass(renderPassInfo, submitNodeCollector);
        SynthesizerRenderState renderState = renderPassInfo.renderState();
        SynthesizerBlockEntity animatable = renderState.blockEntity;
        if (animatable == null) {
            return;
        }
        float partialTick = renderState.partialTick;
        BakedGeoModel model = renderPassInfo.model();
        try {
            float f = animatable.workTimer.value(partialTick);
            float f1 = 360F * animatable.rollTimer.value(partialTick);
            model.getBone("bone20").orElseThrow().frameSnapshot.setRotZ((float) Math.toRadians(7.5F * Math.sin(animatable.tickCount / 2F) * f));
            model.getBone("plate").orElseThrow().frameSnapshot.setRotX((float) Math.toRadians(70F * animatable.openTimer.value(partialTick)));
            model.getBone("rotateit").orElseThrow().frameSnapshot.setRotY((float) Math.toRadians(f1));
        } catch (Throwable e) {
            e.fillInStackTrace();
        }

        for (int i = 3; i < 9; i++) {
            boolean b = animatable.getItem(i).isEmpty();
            model.getBone("bone_%s".formatted(i - 2)).ifPresent(bone -> bone.frameSnapshot.skipRender(b));
        }
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<SynthesizerRenderState> renderPassInfo) {
        super.adjustRenderPose(renderPassInfo);
        renderPassInfo.poseStack().translate(0, 0, 0.0625F * 0.5F);
    }

    public static class SynthesizerRenderState extends BlockEntityRenderState implements GeoRenderState {
        private final Map<DataTicket<?>, Object> dataMap = new HashMap<>();
        SynthesizerBlockEntity blockEntity;
        float partialTick;

        @Override
        public Map<DataTicket<?>, Object> getDataMap() {
            return this.dataMap;
        }
    }
}
