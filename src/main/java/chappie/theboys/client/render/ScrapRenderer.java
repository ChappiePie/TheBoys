package chappie.theboys.client.render;

import chappie.theboys.TheBoys;
import chappie.theboys.common.items.ScrapItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.HU.geckolib3.model.AnimatedGeoModel;
import software.bernie.HU.geckolib3.renderers.geo.GeoItemRenderer;

import javax.annotation.Nullable;

public class ScrapRenderer extends GeoItemRenderer<ScrapItem> {

    public ScrapRenderer() {
        super(new AnimatedGeoModel<ScrapItem>() {
            @Override
            public ResourceLocation getAnimationFileLocation(ScrapItem accessory) {
                return new ResourceLocation(TheBoys.MODID, "animations/scrap.animation.json");
            }

            @Override
            public ResourceLocation getModelLocation(ScrapItem accessory) {
                return new ResourceLocation(TheBoys.MODID, "geo/scrap.geo.json");
            }

            @Override
            public ResourceLocation getTextureLocation(ScrapItem accessory) {
                return new ResourceLocation(TheBoys.MODID, "textures/items/scrap.png");
            }
        });
    }

    @Override
    public RenderType getRenderType(ScrapItem animatable, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        return RenderType.entityCutoutNoCull(textureLocation);
    }
}
