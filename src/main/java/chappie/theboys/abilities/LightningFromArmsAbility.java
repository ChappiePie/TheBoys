package chappie.theboys.abilities;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import xyz.heroesunited.heroesunited.client.events.SetupAnimEvent;
import xyz.heroesunited.heroesunited.common.abilities.AbilityType;
import xyz.heroesunited.heroesunited.common.abilities.IAbilityClientProperties;
import xyz.heroesunited.heroesunited.common.abilities.JSONAbility;
import xyz.heroesunited.heroesunited.util.HUClientUtil;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;

import java.util.function.Consumer;

public class LightningFromArmsAbility extends JSONAbility {

    public LightningFromArmsAbility(AbilityType type, Player player, JsonObject jsonObject) {
        super(type, player, jsonObject);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register("distance", 1.0F);
    }

    @Override
    public void action(Player player) {
        super.action(player);
        if (getEnabled()) {
            HUPlayerUtil.makeLaserLooking(player, this.distance(), 1);
        }
    }

    public void changeDistance(double d) {
        float distance = this.dataManager.<Float>getValue("distance");
        float result = distance + (float) d;
        if (result <= 30 && result > 0) {
            this.dataManager.set("distance", result);
        }
    }

    private float distance() {
        return Mth.clamp((float) this.player.position().add(0, this.player.getEyeHeight(), 0).distanceTo(this.player.getLookAngle()), 0F, this.dataManager.<Float>getValue("distance"));
    }

    @Override
    public void initializeClient(Consumer<IAbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IAbilityClientProperties() {

            @Override
            public void render(EntityRendererProvider.Context context, PlayerRenderer renderer, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                if (getEnabled()) {
                    for (int i = 0; i < 3; i++) {
                        poseStack.pushPose();
                        renderer.getModel().translateToHand(player.getMainArm(), poseStack);
                        poseStack.scale(0.05F, 0.06F, 0.05F);
                        poseStack.translate(i * (player.getMainArm() == HumanoidArm.LEFT ? 1 : -1), 10, 0);
                        HUClientUtil.renderLightning(player.level.random, poseStack, bufferIn, packedLightIn, distance(), i, HUJsonUtils.getColor(getJsonObject()));
                        poseStack.popPose();
                    }
                }
            }

            @Override
            public void setupAnim(SetupAnimEvent event) {
                if (getEnabled()) {
                    ModelPart modelPart = event.getPlayer().getMainArm() == HumanoidArm.LEFT ? event.getPlayerModel().leftArm : event.getPlayerModel().rightArm;
                    modelPart.xRot = (float) Math.toRadians(event.getPlayer().getXRot() - 90F);
                    modelPart.yRot = event.getPlayerModel().head.yRot;
                    modelPart.zRot = 0.0F;
                }
            }

            @Override
            public boolean renderFirstPersonArm(EntityModelSet modelSet, PlayerRenderer renderer, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, AbstractClientPlayer player, HumanoidArm side) {
                if (getEnabled()) {
                    poseStack.pushPose();
                    int sideIncrement = side == HumanoidArm.LEFT ? 1 : -1;
                    ModelPart modelPart = side == HumanoidArm.LEFT ? renderer.getModel().leftArm : renderer.getModel().rightArm;

                    modelPart.xRot = (float) Math.toRadians(-6.0F);
                    modelPart.yRot = (float) Math.toRadians(5.0F * sideIncrement);
                    modelPart.zRot = (float) Math.toRadians(34.0F * sideIncrement);

                    modelPart.x += 5.0F * sideIncrement;
                    modelPart.z -= 2.5F;

                    renderer.getModel().translateToHand(side, poseStack);
                    for (int i = 0; i < 3; i++) {
                        poseStack.pushPose();
                        poseStack.scale(0.05F, 0.06F, 0.05F);
                        poseStack.translate(i * sideIncrement, 10, 0);
                        HUClientUtil.renderLightning(player.level.random, poseStack, bufferIn, packedLightIn, distance(), i, HUJsonUtils.getColor(getJsonObject()));
                        poseStack.popPose();
                    }
                    poseStack.popPose();
                }
                return true;
            }
        });
    }
}
