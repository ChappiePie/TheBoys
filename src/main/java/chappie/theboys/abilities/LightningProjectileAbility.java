package chappie.theboys.abilities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.util.HandSide;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.heroesunited.heroesunited.client.events.HUSetRotationAnglesEvent;
import xyz.heroesunited.heroesunited.common.abilities.JSONAbility;
import xyz.heroesunited.heroesunited.util.HUClientUtil;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;

public class LightningProjectileAbility extends JSONAbility {

    public LightningProjectileAbility() {
        super(TBAbilityTypes.LIGHTNING_PROJECTILE);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setRotationAngles(HUSetRotationAnglesEvent event) {
        super.setRotationAngles(event);
        if (enabled) {
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
            HUClientUtil.copyAnglesToWear(event.getPlayerModel());
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderFirstPersonArm(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, HandSide side) {
        if (enabled) {
            HUClientUtil.drawArmWithLightning(matrix, bufferIn, renderer, player, side, 4, packedLightIn, HUJsonUtils.getColor(this.getJsonObject()));
        }
    }
}
