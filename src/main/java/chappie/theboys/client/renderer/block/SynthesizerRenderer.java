package chappie.theboys.client.renderer.block;

import chappie.theboys.TheBoys;
import chappie.theboys.common.block.entity.SynthesizerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import java.util.HashMap;
import java.util.Map;

public class SynthesizerRenderer extends GeoBlockRenderer<SynthesizerBlockEntity, SynthesizerRenderer.SynthesizerRenderState> {

    public SynthesizerRenderer() {
        super(new DefaultedBlockGeoModel<>(TheBoys.id("synthesizer")));
        this.withScale(0.85F);
    }

    @Override
    public SynthesizerRenderState createRenderState() {
        return new SynthesizerRenderState();
    }

    @Override
    public SynthesizerRenderState captureDefaultRenderState(SynthesizerBlockEntity animatable, Void relatedObject, SynthesizerRenderState renderState, float partialTick) {
        SynthesizerRenderState state = super.captureDefaultRenderState(animatable, relatedObject, renderState, partialTick);
        state.blockEntity = animatable;
        state.partialTick = partialTick;
        return state;
    }

    @Override
    public void preRender(SynthesizerRenderState renderState, PoseStack poseStack, BakedGeoModel model, SubmitNodeCollector renderTasks, CameraRenderState cameraRenderState, int packedLight, int packedOverlay, int renderColor) {
        super.preRender(renderState, poseStack, model, renderTasks, cameraRenderState, packedLight, packedOverlay, renderColor);
        SynthesizerBlockEntity animatable = renderState.blockEntity;
        if (animatable == null) {
            return;
        }
        float partialTick = renderState.partialTick;
        try {
            float f = animatable.workTimer.value(partialTick);
            float f1 = 360F * animatable.rollTimer.value(partialTick);
            model.getBone("bone20").orElseThrow().setRotZ((float) Math.toRadians(7.5F * Math.sin(animatable.tickCount / 2F) * f));
            model.getBone("plate").orElseThrow().setRotX((float) Math.toRadians(70F * animatable.openTimer.value(partialTick)));
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

    public static class SynthesizerRenderState extends BlockEntityRenderState implements GeoRenderState {
        private final Map<DataTicket<?>, Object> dataMap = new HashMap<>();
        SynthesizerBlockEntity blockEntity;
        float partialTick;

        @Override
        public <D> void addGeckolibData(DataTicket<D> dataTicket, D data) {
            this.dataMap.put(dataTicket, data);
        }

        @Override
        public boolean hasGeckolibData(DataTicket<?> dataTicket) {
            return this.dataMap.containsKey(dataTicket);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <D> D getGeckolibData(DataTicket<D> dataTicket) {
            return (D) this.dataMap.get(dataTicket);
        }

        @Override
        public <D> D getOrDefaultGeckolibData(DataTicket<D> dataTicket, D defaultValue) {
            return this.hasGeckolibData(dataTicket) ? this.getGeckolibData(dataTicket) : defaultValue;
        }

        @Override
        public int getPackedLight() {
            return this.lightCoords;
        }

        @Override
        public float getPartialTick() {
            return this.partialTick;
        }

        @Override
        public Map<DataTicket<?>, Object> getDataMap() {
            return this.dataMap;
        }
    }
}
