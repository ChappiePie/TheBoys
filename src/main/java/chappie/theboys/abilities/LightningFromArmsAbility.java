package chappie.theboys.abilities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import xyz.heroesunited.heroesunited.client.events.SetupAnimEvent;
import xyz.heroesunited.heroesunited.common.abilities.IAbilityClientProperties;
import xyz.heroesunited.heroesunited.common.abilities.JSONAbility;
import xyz.heroesunited.heroesunited.util.HUClientUtil;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;

import java.util.function.Consumer;

public class LightningFromArmsAbility extends JSONAbility {

    public LightningFromArmsAbility() {
        super(TBAbilityTypes.LIGHTNING_FROM_ARMS);
    }

    @Override
    public void action(Player player) {
        super.action(player);
        if (getEnabled()) {
            HUPlayerUtil.makeLaserLooking(player, 3, 1);
        }
    }

    @Override
    public void initializeClient(Consumer<IAbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IAbilityClientProperties() {

            @Override
            public void render(EntityRendererProvider.Context context, PlayerRenderer renderer, PoseStack matrix, MultiBufferSource bufferIn, int packedLightIn, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                if (getEnabled()) {
                    Vec3 vec = new Vec3(Math.max(4, player.getLookAngle().x), Math.max(4, player.getLookAngle().y), Math.max(4, player.getLookAngle().z));
                    double distance = player.position().add(0, player.getEyeHeight(), 0).distanceTo(vec);
                    for (int i = 0; i < 3; i++) {
                        matrix.pushPose();
                        renderer.getModel().translateToHand(player.getMainArm(), matrix);
                        matrix.scale(0.05F, 0.06F, 0.05F);
                        matrix.translate(i * (player.getMainArm() == HumanoidArm.LEFT ? 1 : -1), 10, 0);
                        HUClientUtil.renderLightning(player.level.random, matrix, bufferIn, packedLightIn, distance, i, HUJsonUtils.getColor(getJsonObject()));
                        matrix.popPose();
                    }
                }
            }

            @Override
            public void setupAnim(SetupAnimEvent event) {
                if (getEnabled()) {
                    if (event.getPlayer().getMainArm() == HumanoidArm.RIGHT) {
                        event.getPlayerModel().rightArm.xRot = (float) Math.toRadians(event.getPlayer().getXRot() - 90);

                        event.getPlayerModel().rightArm.yRot = event.getPlayerModel().head.yRot;
                        event.getPlayerModel().rightArm.zRot = 0;

                        event.getPlayerModel().rightSleeve.xRot = event.getPlayerModel().rightArm.xRot;
                    } else {
                        event.getPlayerModel().leftArm.xRot = (float) Math.toRadians(event.getPlayer().getXRot() - 90);

                        event.getPlayerModel().leftArm.yRot = event.getPlayerModel().head.yRot;
                        event.getPlayerModel().leftArm.zRot = 0;

                        event.getPlayerModel().leftSleeve.xRot = event.getPlayerModel().leftArm.xRot;
                    }
                }
                HUClientUtil.copyAnglesToWear(event.getPlayerModel());
            }

            @Override
            public boolean renderFirstPersonArm(EntityModelSet modelSet, PlayerRenderer renderer, PoseStack matrix, MultiBufferSource bufferIn, int packedLightIn, AbstractClientPlayer player, HumanoidArm side) {
                if (getEnabled()) {
                    matrix.pushPose();
                    ModelPart modelRenderer = side == HumanoidArm.LEFT ? renderer.getModel().leftArm : renderer.getModel().rightArm;
                    modelRenderer.xRot = modelRenderer.yRot = modelRenderer.zRot = 0;
                    renderer.getModel().translateToHand(side, matrix);
                    for (int i = 0; i < 3; i++) {
                        matrix.pushPose();
                        matrix.scale(0.05F, 0.06F, 0.05F);
                        matrix.translate(i * (side == HumanoidArm.LEFT ? 1 : -1), 10, 0);
                        HUClientUtil.renderLightning(player.level.random, matrix, bufferIn, packedLightIn, 4, i, HUJsonUtils.getColor(getJsonObject()));
                        matrix.popPose();
                    }
                    matrix.popPose();
                }
                return true;
            }
        });
    }
}
