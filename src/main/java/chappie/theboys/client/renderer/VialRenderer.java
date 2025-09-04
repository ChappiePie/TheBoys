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
import software.bernie.geckolib.util.RenderUtils;

import java.util.Optional;

public class VialRenderer extends GeoItemRenderer<VialItem> {

    public VialRenderer() {
        super(new DefaultedItemGeoModel<VialItem>(TheBoys.id("vial")).withAltTexture(TheBoys.id("syringe/3d")));
    }

    @Override
    public RenderType getRenderType(VialItem animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, VialItem animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (!bone.getName().equals("bone3")) {
            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }

    @Override
    public void preRender(PoseStack poseStack, VialItem animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        Optional<GeoBone> boneOptional = model.getBone("bone3");
        int color = -1;
        if (this.currentItemStack.getItem() instanceof VialItem item) {
            color = item.getColor(this.currentItemStack);
        }
        if (boneOptional.isPresent() && color != -1) {
            float r = ClientUtil.ARGB.red(color) / 255F;
            float g = ClientUtil.ARGB.green(color) / 255F;
            float b = ClientUtil.ARGB.blue(color) / 255F;
            GeoBone bone = boneOptional.get();
            VertexConsumer vertexConsumer = ModelBakery.WATER_FLOW.buffer(bufferSource, ClientUtil.ModRenderTypes::glow);
            poseStack.pushPose();
            RenderUtils.prepMatrixForBone(poseStack, bone.getParent().getParent());
            RenderUtils.prepMatrixForBone(poseStack, bone.getParent());
            RenderUtils.prepMatrixForBone(poseStack, bone);
            for (GeoCube cube : bone.getCubes()) {
                poseStack.pushPose();
                renderCube(poseStack, cube, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
                poseStack.popPose();
            }
            bufferSource.getBuffer(this.getRenderType(animatable, this.getTextureLocation(animatable), bufferSource, partialTick));
            poseStack.popPose();
        }
    }
}
