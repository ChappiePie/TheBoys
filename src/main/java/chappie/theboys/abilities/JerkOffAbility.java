package chappie.theboys.abilities;

import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.heroesunited.heroesunited.client.events.HUSetRotationAnglesEvent;
import xyz.heroesunited.heroesunited.common.abilities.JSONAbility;
import xyz.heroesunited.heroesunited.util.HUClientUtil;

public class JerkOffAbility extends JSONAbility {

    public JerkOffAbility() {
        super(TBAbilityTypes.JERK_OFF);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setRotationAngles(HUSetRotationAnglesEvent event) {
        if (this.enabled) {
            float f = MathHelper.cos(event.getAgeInTicks()) * 24;
            float rotationX = (float) Math.toRadians(-(event.getPlayer().isCrouching() ? 5F - f : 30F + f));
            if (event.getPlayer().getMainArm() == HandSide.RIGHT) {
                event.getPlayerModel().rightArm.xRot = rotationX;
                event.getPlayerModel().rightArm.zRot = (float) Math.toRadians(-45F);
            } else {
                event.getPlayerModel().leftArm.xRot = rotationX;
                event.getPlayerModel().leftArm.zRot = (float) Math.toRadians(45F);
            }
            HUClientUtil.copyAnglesToWear(event.getPlayerModel());
        }
    }
}
