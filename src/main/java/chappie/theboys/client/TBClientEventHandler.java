package chappie.theboys.client;

import chappie.theboys.abilities.ScrollAbility;
import chappie.theboys.abilities.TBAbilityTypes;
import chappie.theboys.network.TBNetworking;
import chappie.theboys.network.server.ServerScrollAbility;
import chappie.theboys.util.ISimpleSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.heroesunited.heroesunited.client.events.RenderLayerEvent;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;
import xyz.heroesunited.heroesunited.common.abilities.FlightAbility;
import xyz.heroesunited.heroesunited.util.HUClientUtil;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;

public class TBClientEventHandler {

    @SubscribeEvent
    public void playSound(PlaySoundEvent event) {
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (event.getSound() instanceof SimpleSoundInstance soundInstance && camera.getEntity() instanceof Player player) {
            if (AbilityHelper.getAbilities(player).stream().anyMatch(p -> p.type == TBAbilityTypes.SUPER_HEARING && p.getEnabled())) {
                Vec3 vec3 = new Vec3(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ());
                double distance = vec3.distanceTo(player.position());
                if (distance < 40) {
                    ((ISimpleSoundInstance) soundInstance).setPosition(player.position());
                    event.setSound(soundInstance);
                }
            }
        }
    }

    @SubscribeEvent
    public void mouseScroll(InputEvent.MouseScrollEvent event) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            for (var ability : AbilityHelper.getListOfType(ScrollAbility.class, AbilityHelper.getAbilities(player))) {
                if (ability.getKey() != 0 && ability.getEnabled()) {
                    ability.consumer.accept(ability, event.getScrollDelta());
                    TBNetworking.INSTANCE.sendToServer(new ServerScrollAbility(ability.name, event.getScrollDelta()));
                    event.setCanceled(true);
                }
            }
        }
    }

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