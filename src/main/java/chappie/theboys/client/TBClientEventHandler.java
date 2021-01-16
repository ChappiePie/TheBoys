package chappie.theboys.client;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.heroesunited.heroesunited.client.events.HURenderLayerEvent;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;
import xyz.heroesunited.heroesunited.common.abilities.FlightAbility;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayer;
import xyz.heroesunited.heroesunited.util.HUClientUtil;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;

public class TBClientEventHandler {

    @SubscribeEvent
    public void renderPlayerLayers(HURenderLayerEvent.Player event) {
        AbstractClientPlayerEntity player = event.getPlayer();
        for (Ability ability : AbilityHelper.getAbilities(player)) {
            if (ability instanceof FlightAbility && HUPlayer.getCap(player).isFlying() && !player.isOnGround() && JSONUtils.getString(ability.getJsonObject(), "type", "").equals("lightnings")) {
                double y = (((player.getPosY() !=0 ? player.getPosY() : 5) / player.getHeight()) + player.getHeight());
                HUClientUtil.drawArmWithLightning(event.getMatrixStack(), event.getBuffers(), event.getRenderer(), player, HandSide.LEFT, y, event.getLight(), HUJsonUtils.getColor(ability.getJsonObject()));
                HUClientUtil.drawArmWithLightning(event.getMatrixStack(), event.getBuffers(), event.getRenderer(), player, HandSide.RIGHT, y, event.getLight(), HUJsonUtils.getColor(ability.getJsonObject()));
            }
        }
    }

}