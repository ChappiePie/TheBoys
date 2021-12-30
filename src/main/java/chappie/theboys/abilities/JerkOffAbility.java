package chappie.theboys.abilities;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import xyz.heroesunited.heroesunited.client.events.SetupAnimEvent;
import xyz.heroesunited.heroesunited.common.abilities.IAbilityClientProperties;
import xyz.heroesunited.heroesunited.common.abilities.JSONAbility;

import java.util.function.Consumer;

public class JerkOffAbility extends JSONAbility {

    public JerkOffAbility() {
        super(TBAbilityTypes.JERK_OFF);
    }

    @Override
    public void initializeClient(Consumer<IAbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IAbilityClientProperties() {
            @Override
            public void setupAnim(SetupAnimEvent event) {
                if (getEnabled()) {
                    float f = Mth.cos(event.getAgeInTicks()) * 24;
                    float rotationX = (float) Math.toRadians(-(event.getPlayer().isCrouching() ? 5F - f : 30F + f));
                    if (event.getPlayer().getMainArm() == HumanoidArm.RIGHT) {
                        event.getPlayerModel().rightArm.xRot = rotationX;
                        event.getPlayerModel().rightArm.zRot = (float) Math.toRadians(-45F);
                    } else {
                        event.getPlayerModel().leftArm.xRot = rotationX;
                        event.getPlayerModel().leftArm.zRot = (float) Math.toRadians(45F);
                    }
                }
            }
        });
    }
}
