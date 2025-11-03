package chappie.theboys.client.renderer;

import chappie.theboys.TheBoys;
import chappie.theboys.common.item.VialItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class VialRenderer extends GeoItemRenderer<VialItem> {

    private ItemStack currentStack = ItemStack.EMPTY;
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
        this.currentStack = renderData.itemStack();
        this.fluidColor = this.currentStack.getItem() instanceof VialItem ? VialItem.getColor(this.currentStack) : -1;
    }

    @Override
    public void renderBone(GeoRenderState renderState, PoseStack poseStack, GeoBone bone, VertexConsumer buffer, CameraRenderState cameraState, int packedLight, int packedOverlay, int renderColor) {
        if ("bone3".equals(bone.getName())) {
            int color = this.fluidColor != -1 ? ARGB.color(ARGB.alpha(renderColor), this.fluidColor) : renderColor;
            super.renderBone(renderState, poseStack, bone, buffer, cameraState, packedLight, packedOverlay, color);
            return;
        }
        super.renderBone(renderState, poseStack, bone, buffer, cameraState, packedLight, packedOverlay, renderColor);
    }
}
