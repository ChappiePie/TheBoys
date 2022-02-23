package chappie.theboys.abilities;

import com.google.gson.JsonObject;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import xyz.heroesunited.heroesunited.client.events.SetupAnimEvent;
import xyz.heroesunited.heroesunited.common.abilities.AbilityType;
import xyz.heroesunited.heroesunited.common.abilities.IAbilityClientProperties;
import xyz.heroesunited.heroesunited.common.abilities.JSONAbility;

import java.util.function.Consumer;

/** Basically just an ability for a joke, it's not serious, man... */
public class JerkOffAbility extends JSONAbility {

    public JerkOffAbility(AbilityType type, Player player, JsonObject jsonObject) {
        super(type, player, jsonObject);
    }

    @Override
    public void initializeClient(Consumer<IAbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IAbilityClientProperties() {
            @Override
            public void setupAnim(SetupAnimEvent event) {
                if (getEnabled()) {
                    float f = Mth.cos(event.getAgeInTicks()) * 12.0F;
                    boolean left = event.getPlayer().getMainArm() == HumanoidArm.LEFT;
                    ModelPart part = left ? event.getPlayerModel().leftArm : event.getPlayerModel().rightArm;
                    part.xRot = (float) -Math.toRadians(event.getPlayer().isCrouching() ? 5.0F - f : 30.0F + f);
                    part.zRot = (float) Math.toRadians(left ? 45.0F : -45.0F);
                }
            }
        });
    }
}
