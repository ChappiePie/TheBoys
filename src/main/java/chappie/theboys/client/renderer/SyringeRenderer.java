package chappie.theboys.client.renderer;

import chappie.modulus.util.IHasTimer;
import chappie.theboys.TheBoys;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.item.SyringeItem;
import chappie.theboys.common.item.datacomponents.TBDataComponents;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SyringeRenderer extends GeoItemRenderer<SyringeItem> {

    private static final Identifier WATER_FLOW_TEXTURE = Identifier.withDefaultNamespace("block/water_flow");

    public final IHasTimer.Timer timeline = new IHasTimer.Timer(() -> 450, () -> false);
    private ItemStack currentStack = ItemStack.EMPTY;
    private int fluidColor = -1;

    public SyringeRenderer() {
        super(new DefaultedItemGeoModel<SyringeItem>(TheBoys.id("syringe")).withAltTexture(TheBoys.id("syringe/3d")));
    }

    @Override
    public RenderType getRenderType(GeoRenderState renderState, Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    @Override
    public void addRenderData(SyringeItem animatable, RenderData renderData, GeoRenderState renderState, float partialTick) {
        ItemStack stack = renderData.itemStack();
        this.currentStack = stack;
        this.fluidColor = stack.getItem() instanceof SyringeItem ? SyringeItem.getColor(stack) : -1;

        this.timeline.predicate = () -> {
            if (Minecraft.getInstance().getCameraEntity() instanceof Player player) {
                TheBoysCap cap = TheBoysCap.getCap(player);
                boolean b = cap != null && cap.syringeAnim.timeline.value(partialTick) > 0.3F;
                boolean b1 = player.isUsingItem();
                return b && b1;
            }
            return false;
        };
        if (!Minecraft.getInstance().isPaused()) {
            this.timeline.update();
        }
    }

    @Override
    public void preRenderPass(RenderPassInfo<GeoRenderState> renderPassInfo, SubmitNodeCollector submitNodeCollector) {
        super.preRenderPass(renderPassInfo, submitNodeCollector);

        // Hide/show vial bone based on whether a vial is inserted
        ItemStack vialStack = this.currentStack.getOrDefault(TBDataComponents.VIAL, ItemStack.EMPTY);
        renderPassInfo.model().getBone("bone2").ifPresent(bone ->
                bone.frameSnapshot.skipRender(vialStack.isEmpty()));

        // Hide bone3 from normal rendering — we'll render it custom in submitRenderTasks
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
            float scale = Math.max(0.0F, 1.0F - this.timeline.value(renderPassInfo.renderState().getPartialTick()));

            PoseStack poseStack = renderPassInfo.poseStack();
            renderTasks.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(sprite.atlasLocation()), (pose, vertexConsumer) -> {
                VertexConsumer spriteBuffer = sprite.wrap(vertexConsumer);
                PoseStack cubeStack = new PoseStack();
                cubeStack.last().pose().set(pose.pose());
                cubeStack.last().normal().set(pose.normal());

                // Transform to bone position
                bone.translateToPivotPoint(cubeStack);
                bone.frameSnapshot.setScaleY(scale);
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
