package chappie.theboys.client;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.heroesunited.heroesunited.client.events.RenderLayerEvent;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;
import xyz.heroesunited.heroesunited.common.abilities.FlightAbility;
import xyz.heroesunited.heroesunited.util.HUClientUtil;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;

public class TBClientEventHandler {

    @SubscribeEvent
    public void renderPlayerLayers(RenderLayerEvent.Player event) {
        AbstractClientPlayer player = event.getPlayer();
        for (Ability ability : AbilityHelper.getAbilities(player)) {
            if (ability instanceof FlightAbility && ((FlightAbility) ability).isFlying(player) && !player.isOnGround() && GsonHelper.getAsString(ability.getJsonObject(), "type", "").equals("lightnings")) {
                HUClientUtil.drawArmWithLightning(event.getPoseStack(), event.getMultiBufferSource(), event.getRenderer(), player, HumanoidArm.LEFT, 4, event.getPackedLight(), HUJsonUtils.getColor(ability.getJsonObject()));
                HUClientUtil.drawArmWithLightning(event.getPoseStack(), event.getMultiBufferSource(), event.getRenderer(), player, HumanoidArm.RIGHT, 4, event.getPackedLight(), HUJsonUtils.getColor(ability.getJsonObject()));
            }
        }
    }

}