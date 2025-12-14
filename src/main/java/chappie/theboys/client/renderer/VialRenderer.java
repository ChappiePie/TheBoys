package chappie.theboys.client.renderer;

import chappie.theboys.TheBoys;
import chappie.theboys.common.item.VialItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderModelPositioner;

import static net.minecraft.client.resources.model.ModelBakery.WATER_FLOW;

public class VialRenderer extends GeoItemRenderer<VialItem> {

    private int fluidColor = -1;

    public VialRenderer() {
        super(new DefaultedItemGeoModel<VialItem>(TheBoys.id("vial")).withAltTexture(TheBoys.id("syringe/3d")));
    }

    @Override
    public RenderType getRenderType(GeoRenderState renderState, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void addRenderData(VialItem animatable, RenderData renderData, GeoRenderState renderState, float partialTick) {
        this.fluidColor = VialItem.getColor(renderData.itemStack());
    }

    @Override
    public void renderBone(GeoRenderState renderState, PoseStack poseStack, GeoBone bone, VertexConsumer buffer, CameraRenderState cameraState, int packedLight, int packedOverlay, int renderColor) {
        if ("bone3".equals(bone.getName())) {
            return;
        }

        super.renderBone(renderState, poseStack, bone, buffer, cameraState, packedLight, packedOverlay, renderColor);
    }

    @Override
    public void buildRenderTask(GeoRenderState renderState, PoseStack poseStack, BakedGeoModel bakedModel, GeoModel<VialItem> model, OrderedSubmitNodeCollector renderTasks, CameraRenderState cameraState, @Nullable RenderType renderType, int packedLight, int packedOverlay, int renderColor, @Nullable RenderModelPositioner<GeoRenderState> modelPositioner) {
        super.buildRenderTask(renderState, poseStack, bakedModel, model, renderTasks, cameraState, renderType, packedLight, packedOverlay, renderColor, modelPositioner);
        model.getBone("bone3").ifPresent(bone -> {
            if (this.fluidColor == -1) {
                bone.setHidden(true);
                return;
            }

            bone.setHidden(false);
            TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(WATER_FLOW.texture());
            renderTasks.submitCustomGeometry(poseStack, RenderType.entityTranslucent(sprite.atlasLocation()), (pose, vertexConsumer) -> {
                VertexConsumer spriteBuffer = sprite.wrap(vertexConsumer);
                PoseStack cubeStack = new PoseStack();
                cubeStack.last().set(pose);
                bone.transformToBone(cubeStack);

                for (GeoCube cube : bone.getCubes()) {
                    cubeStack.pushPose();
                    renderCube(renderState, cube, cubeStack, spriteBuffer, cameraState, LightTexture.FULL_BRIGHT, packedOverlay, ARGB.color(ARGB.alpha(renderColor), this.fluidColor));
                    cubeStack.popPose();
                }
            });
        });
    }
}
