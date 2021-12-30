package chappie.theboys.abilities;

import com.google.gson.JsonArray;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import xyz.heroesunited.heroesunited.client.renderer.IHUModelPart;
import xyz.heroesunited.heroesunited.common.abilities.IAbilityClientProperties;
import xyz.heroesunited.heroesunited.common.abilities.JSONAbility;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;
import xyz.heroesunited.heroesunited.util.PlayerPart;

import java.util.function.Consumer;

public class OverlayAbility extends JSONAbility {

    public OverlayAbility() {
        super(TBAbilityTypes.OVERLAY);
    }

    @Override
    public void initializeClient(Consumer<IAbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IAbilityClientProperties() {
            @Override
            public void render(EntityRendererProvider.Context context, PlayerRenderer renderer, PoseStack matrix, MultiBufferSource bufferIn, int packedLightIn, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                if (getEnabled()) {
                    ResourceLocation location = new ResourceLocation(GsonHelper.getAsString(getJsonObject(), "texture"));
                    VertexConsumer builder = bufferIn.getBuffer(RenderType.entityTranslucent(location));
                    ModelPart mainPart = context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR);
                    PlayerModel<AbstractClientPlayer> model = new PlayerModel<>(mainPart, HUPlayerUtil.haveSmallArms(player));
                    mainPart.getAllParts().filter((p_170824_) -> !p_170824_.isEmpty()).forEach(part -> ((IHUModelPart) (Object) part).setSize(new CubeDeformation(GsonHelper.getAsFloat(getJsonObject(), "inflate", 0))));
                    renderer.getModel().copyPropertiesTo(model);
                    model.jacket.copyFrom(renderer.getModel().jacket);
                    model.rightSleeve.copyFrom(renderer.getModel().rightSleeve);
                    model.leftSleeve.copyFrom(renderer.getModel().leftSleeve);
                    model.rightPants.copyFrom(renderer.getModel().rightPants);
                    model.leftPants.copyFrom(renderer.getModel().leftPants);

                    JsonArray overrides = GsonHelper.getAsJsonArray(getJsonObject(), "visibility_parts");
                    model.setAllVisible(false);
                    for (int i = 0; i < overrides.size(); i++) {
                        PlayerPart part = PlayerPart.byName(overrides.get(i).getAsString());
                        if (part != null) {
                            part.setVisibility(model, true);
                        }
                    }
                    model.renderToBuffer(matrix, builder, packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
                }
            }
        });
    }
}
