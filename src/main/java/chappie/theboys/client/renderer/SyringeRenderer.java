package chappie.theboys.client.renderer;

import chappie.modulus.util.IHasTimer;
import chappie.theboys.TheBoys;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.item.SyringeItem;
import chappie.theboys.common.item.datacomponents.TBDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class SyringeRenderer extends GeoItemRenderer<SyringeItem> {

    public final IHasTimer.Timer timeline = new IHasTimer.Timer(() -> 150, () -> false);
    private ItemStack currentStack = ItemStack.EMPTY;
    private int fluidColor = -1;

    public SyringeRenderer() {
        super(new DefaultedItemGeoModel<SyringeItem>(TheBoys.id("syringe")).withAltTexture(TheBoys.id("syringe/3d")));
    }

    @Override
    public RenderType getRenderType(GeoRenderState renderState, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void addRenderData(SyringeItem animatable, RenderData renderData, GeoRenderState renderState, float partialTick) {
        ItemStack stack = renderData.itemStack();
        this.currentStack = stack;
        this.fluidColor = stack.getItem() instanceof SyringeItem ? SyringeItem.getColor(stack) : -1;

        ItemStack vialStack = stack.getOrDefault(TBDataComponents.VIAL, ItemStack.EMPTY);
        this.getGeoModel().getBone("bone2").ifPresent(bone -> bone.setHidden(vialStack.isEmpty()));

        this.timeline.predicate = () -> {
            if (Minecraft.getInstance().getCameraEntity() instanceof Player player) {
                TheBoysCap cap = TheBoysCap.getCap(player);
                return cap != null && cap.syringeAnim.timeline.value(partialTick) > 0.3F && player.isUsingItem();
            }
            return false;
        };
        if (!Minecraft.getInstance().isPaused()) {
            this.timeline.update();
        }
    }

    @Override
    public void renderBone(GeoRenderState renderState, PoseStack poseStack, GeoBone bone, VertexConsumer buffer, CameraRenderState cameraState, int packedLight, int packedOverlay, int renderColor) {
        String name = bone.getName();
        if ("bone2".equals(name)) {
            boolean hide = this.currentStack.getOrDefault(TBDataComponents.VIAL, ItemStack.EMPTY).isEmpty();
            bone.setHidden(hide);
        }

        if ("bone3".equals(name)) {
            float previousScale = bone.getScaleY();
            float scale = Math.max(0.0F, 1.0F - this.timeline.value(renderState.getPartialTick()));
            bone.setScaleY(scale);
            int color = this.fluidColor != -1 ? ARGB.color(ARGB.alpha(renderColor), this.fluidColor) : renderColor;
            super.renderBone(renderState, poseStack, bone, buffer, cameraState, packedLight, packedOverlay, color);
            bone.setScaleY(previousScale);
            return;
        }

        super.renderBone(renderState, poseStack, bone, buffer, cameraState, packedLight, packedOverlay, renderColor);
    }
}
