package chappie.theboys.abilities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.heroesunited.heroesunited.client.events.HUSetRotationAnglesEvent;
import xyz.heroesunited.heroesunited.common.abilities.JSONAbility;
import xyz.heroesunited.heroesunited.util.HUClientUtil;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;

public class LightningFromArmsAbility extends JSONAbility {

    public LightningFromArmsAbility() {
        super(TBAbilityTypes.LIGHTNING_FROM_ARMS);
    }

    @Override
    public void action(PlayerEntity player) {
        super.action(player);
        if (getEnabled()) {
            HUPlayerUtil.makeLaserLooking(player, 3, 1);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (getEnabled()) {
            Vector3d vec = new Vector3d(Math.max(4, player.getLookAngle().x), Math.max(4, player.getLookAngle().y), Math.max(4, player.getLookAngle().z));
            double distance = player.position().add(0, player.getEyeHeight(), 0).distanceTo(vec);
            for (int i = 0; i < 3; i++) {
                matrix.pushPose();
                renderer.getModel().translateToHand(player.getMainArm(), matrix);
                matrix.scale(0.05F, 0.06F, 0.05F);
                matrix.translate(i * (player.getMainArm() == HandSide.LEFT ? 1 : -1), 10, 0);
                HUClientUtil.renderLightning(player.level.random, matrix, bufferIn, packedLightIn, distance, i, HUJsonUtils.getColor(this.getJsonObject()));
                matrix.popPose();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setRotationAngles(HUSetRotationAnglesEvent event) {
        super.setRotationAngles(event);
        if (getEnabled()) {
            if (event.getPlayer().getMainArm() == HandSide.RIGHT) {
                event.getPlayerModel().rightArm.xRot = (float) Math.toRadians(event.getPlayer().xRot - 90);

                event.getPlayerModel().rightArm.yRot = event.getPlayerModel().head.yRot;
                event.getPlayerModel().rightArm.zRot = 0;

                event.getPlayerModel().rightSleeve.xRot = event.getPlayerModel().rightArm.xRot;
            } else {
                event.getPlayerModel().leftArm.xRot = (float) Math.toRadians(event.getPlayer().xRot - 90);

                event.getPlayerModel().leftArm.yRot = event.getPlayerModel().head.yRot;
                event.getPlayerModel().leftArm.zRot = 0;

                event.getPlayerModel().leftSleeve.xRot = event.getPlayerModel().leftArm.xRot;
            }
        }
        HUClientUtil.copyAnglesToWear(event.getPlayerModel());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderFirstPersonArm(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, HandSide side) {
        if (getEnabled()) {
            matrix.pushPose();
            ModelRenderer modelRenderer = side == HandSide.LEFT ? renderer.getModel().leftArm : renderer.getModel().rightArm;
            modelRenderer.xRot = modelRenderer.yRot = modelRenderer.zRot = 0;
            renderer.getModel().translateToHand(side, matrix);
            for (int i = 0; i < 3; i++) {
                matrix.pushPose();
                matrix.scale(0.05F, 0.06F, 0.05F);
                matrix.translate(i * (side == HandSide.LEFT ? 1 : -1), 10, 0);
                HUClientUtil.renderLightning(player.level.random, matrix, bufferIn, packedLightIn, 4, i, HUJsonUtils.getColor(this.getJsonObject()));
                matrix.popPose();
            }
            matrix.popPose();
        }
    }
}
