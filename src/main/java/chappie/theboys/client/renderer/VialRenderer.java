package chappie.theboys.client.renderer;

import chappie.modulus.util.ClientUtil;
import chappie.theboys.TheBoys;
import chappie.theboys.common.item.VialItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.Optional;

public class VialRenderer extends GeoItemRenderer<VialItem> {

    public VialRenderer() {
        super(new DefaultedItemGeoModel<VialItem>(ResourceLocation.fromNamespaceAndPath(TheBoys.MODID, "vial")).withAltTexture(ResourceLocation.fromNamespaceAndPath(TheBoys.MODID, "syringe/3d")));
    }

    @Override
    public RenderType getRenderType(VialItem animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, VialItem animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int renderColor) {
        if (!bone.getName().equals("bone3")) {
            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, renderColor);
        }
    }

    @Override
    public void preRender(PoseStack poseStack, VialItem animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int renderColor) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, renderColor);
        Optional<GeoBone> boneOptional = model.getBone("bone3");
        int color = -1;
        if (this.currentItemStack.getItem() instanceof VialItem) {
            color = VialItem.getColor(this.currentItemStack);
        }
        if (boneOptional.isPresent() && color != -1) {
            GeoBone bone = boneOptional.get();
            VertexConsumer vertexConsumer = ModelBakery.WATER_FLOW.buffer(bufferSource, ClientUtil.ModRenderTypes::glow);
            poseStack.pushPose();
            RenderUtil.prepMatrixForBone(poseStack, bone.getParent().getParent());
            RenderUtil.prepMatrixForBone(poseStack, bone.getParent());
            RenderUtil.prepMatrixForBone(poseStack, bone);
            for (GeoCube cube : bone.getCubes()) {
                poseStack.pushPose();
                renderCube(poseStack, cube, vertexConsumer, packedLight, packedOverlay, ClientUtil.ARGB.color(ClientUtil.ARGB.alpha(renderColor), color));
                poseStack.popPose();
            }
            bufferSource.getBuffer(this.getRenderType(animatable, this.getTextureLocation(animatable), bufferSource, partialTick));
            poseStack.popPose();
        }
    }
}
