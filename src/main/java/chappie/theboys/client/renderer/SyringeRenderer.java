package chappie.theboys.client.renderer;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.IHasTimer;
import chappie.theboys.TheBoys;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.item.SyringeItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtils;

import java.util.Optional;

public class SyringeRenderer extends GeoItemRenderer<SyringeItem> {

    public final IHasTimer.Timer timeline = new IHasTimer.Timer(() -> 150, () -> false);

    public SyringeRenderer() {
        super(new DefaultedItemGeoModel<SyringeItem>(TheBoys.id("syringe")).withAltTexture(TheBoys.id("syringe/3d")));
    }

    @Override
    public RenderType getRenderType(SyringeItem animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, SyringeItem animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (bone.getName().equals("bone2")) {
            bone.setHidden(this.currentItemStack.getTag() == null || !this.currentItemStack.getTag().contains("vial"));
        }

        if (!bone.getName().equals("bone3")) {
            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }

    @Override
    public void preRender(PoseStack poseStack, SyringeItem animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        this.timeline.predicate = () -> {
            if (Minecraft.getInstance().getCameraEntity() instanceof Player player) {
                TheBoysCap cap = TheBoysCap.getCap(player);
                if (cap != null) {
                    return cap.syringeAnim.timeline.value(partialTick) > 0.3 && player.isUsingItem();
                }
            }
            return false;
        };
        Optional<GeoBone> boneOptional = model.getBone("bone3");
        int color = -1;
        if (this.currentItemStack.getItem() instanceof SyringeItem item) {
            color = item.getColor(this.currentItemStack);
        }
        if (boneOptional.isPresent() && color != -1) {
            float r = ClientUtil.ARGB.red(color) / 255F;
            float g = ClientUtil.ARGB.green(color) / 255F;
            float b = ClientUtil.ARGB.blue(color) / 255F;
            GeoBone bone = boneOptional.get();
            VertexConsumer vertexConsumer = ModelBakery.WATER_FLOW.buffer(bufferSource, ClientUtil.ModRenderTypes::glow);
            poseStack.pushPose();
            bone.setScaleY(1F - this.timeline.value(partialTick));
            RenderUtils.prepMatrixForBone(poseStack, bone);
            bone.setScaleY(1F);
            for (GeoCube cube : bone.getCubes()) {
                poseStack.pushPose();
                renderCube(poseStack, cube, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
                poseStack.popPose();
            }
            bufferSource.getBuffer(this.getRenderType(animatable, this.getTextureLocation(animatable), bufferSource, partialTick));
            poseStack.popPose();
        }
        if (!Minecraft.getInstance().isPaused()) {
            this.timeline.update();
        }
    }
}
