package chappie.theboys.client.renderer;

import chappie.theboys.TheBoys;
import chappie.theboys.common.item.VialItem;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.model.DefaultedItemGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;

public class VialRenderer extends GeoItemRenderer<VialItem> {

    private static final Identifier WATER_FLOW_TEXTURE = Identifier.withDefaultNamespace("block/water_flow");

    private int fluidColor = -1;

    public VialRenderer() {
        super(new DefaultedItemGeoModel<VialItem>(TheBoys.id("vial")).withAltTexture(TheBoys.id("syringe/3d")));
    }

    @Override
    public RenderType getRenderType(GeoRenderState renderState, Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    @Override
    public void addRenderData(VialItem animatable, RenderData renderData, GeoRenderState renderState, float partialTick) {
        this.fluidColor = VialItem.getColor(renderData.itemStack());
    }

    @Override
    public void preRenderPass(RenderPassInfo<GeoRenderState> renderPassInfo, SubmitNodeCollector submitNodeCollector) {
        super.preRenderPass(renderPassInfo, submitNodeCollector);

        // Hide bone3 from normal rendering — we render it custom with fluid texture
        renderPassInfo.model().getBone("bone3").ifPresent(bone ->
                bone.frameSnapshot.skipRender(true));
    }

    @Override
    public void submitRenderTasks(RenderPassInfo<GeoRenderState> renderPassInfo, OrderedSubmitNodeCollector renderTasks, RenderType renderType) {
        super.submitRenderTasks(renderPassInfo, renderTasks, renderType);

        renderPassInfo.model().getBone("bone3").ifPresent(bone -> {
            if (this.fluidColor == -1) {
                return;
            }

            TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager()
                    .getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(WATER_FLOW_TEXTURE);
            int color = ARGB.color(255, this.fluidColor);

            PoseStack poseStack = renderPassInfo.poseStack();
            renderTasks.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(sprite.atlasLocation()), (pose, vertexConsumer) -> {
                VertexConsumer spriteBuffer = sprite.wrap(vertexConsumer);
                PoseStack cubeStack = new PoseStack();
                cubeStack.last().pose().set(pose.pose());
                cubeStack.last().normal().set(pose.normal());

                // Transform to bone position
                bone.translateToPivotPoint(cubeStack);
                bone.frameSnapshot.rotate(cubeStack);
                bone.frameSnapshot.translate(cubeStack);
                bone.frameSnapshot.scale(cubeStack);
                bone.translateAwayFromPivotPoint(cubeStack);

                if (bone instanceof CuboidGeoBone cuboidBone) {
                    for (GeoCube cube : cuboidBone.cubes) {
                        cubeStack.pushPose();
                        cube.render(cubeStack, spriteBuffer, LightCoordsUtil.FULL_BRIGHT, 0, color);
                        cubeStack.popPose();
                    }
                }
            });
        });
    }
}
