package chappie.theboys.client;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.heroesunited.heroesunited.client.events.HURenderLayerEvent;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;
import xyz.heroesunited.heroesunited.common.abilities.FlightAbility;
import xyz.heroesunited.heroesunited.util.HUClientUtil;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;

public class TBClientEventHandler {

    @SubscribeEvent
    public void renderPlayerLayers(HURenderLayerEvent.Player event) {
        AbstractClientPlayerEntity player = event.getPlayer();
        for (Ability ability : AbilityHelper.getAbilities(player)) {
            if (ability instanceof FlightAbility && ((FlightAbility) ability).isFlying(player) && !player.isOnGround() && JSONUtils.getAsString(ability.getJsonObject(), "type", "").equals("lightnings")) {
                HUClientUtil.drawArmWithLightning(event.getMatrixStack(), event.getBuffers(), event.getRenderer(), player, HandSide.LEFT, 4, event.getLight(), HUJsonUtils.getColor(ability.getJsonObject()));
                HUClientUtil.drawArmWithLightning(event.getMatrixStack(), event.getBuffers(), event.getRenderer(), player, HandSide.RIGHT, 4, event.getLight(), HUJsonUtils.getColor(ability.getJsonObject()));
            }
        }
    }

}