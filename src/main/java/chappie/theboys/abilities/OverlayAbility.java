package chappie.theboys.abilities;

import com.google.gson.JsonArray;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.heroesunited.heroesunited.common.abilities.JSONAbility;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;
import xyz.heroesunited.heroesunited.util.PlayerPart;

public class OverlayAbility extends JSONAbility {

    public OverlayAbility() {
        super(TBAbilityTypes.OVERLAY);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        super.render(renderer, matrix, bufferIn, packedLightIn, player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        if (getEnabled()) {
            ResourceLocation location = new ResourceLocation(JSONUtils.getAsString(getJsonObject(), "texture"));
            IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityTranslucent(location));
            PlayerModel model = new PlayerModel(JSONUtils.getAsFloat(getJsonObject(), "inflate", 0), HUPlayerUtil.haveSmallArms(player));
            renderer.getModel().copyPropertiesTo(model);
            model.jacket.copyFrom(renderer.getModel().jacket);
            model.rightSleeve.copyFrom(renderer.getModel().rightSleeve);
            model.leftSleeve.copyFrom(renderer.getModel().leftSleeve);
            model.rightPants.copyFrom(renderer.getModel().rightPants);
            model.leftPants.copyFrom(renderer.getModel().leftPants);

            JsonArray overrides = JSONUtils.getAsJsonArray(getJsonObject(), "visibility_parts");
            model.setAllVisible(false);
            for (int i = 0; i < overrides.size(); i++) {
                PlayerPart part = PlayerPart.getByName(overrides.get(i).getAsString());
                if (part != null) {
                    part.setVisibility(model, true);
                }
            }
            model.renderToBuffer(matrix, builder, packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        }
    }
}
