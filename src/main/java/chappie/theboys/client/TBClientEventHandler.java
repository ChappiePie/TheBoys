package chappie.theboys.client;

import chappie.theboys.TheBoys;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.heroesunited.heroesunited.client.events.HUSetRotationAnglesEvent;
import xyz.heroesunited.heroesunited.common.abilities.suit.Suit;
import xyz.heroesunited.heroesunited.common.abilities.suit.SuitItem;

import static net.minecraft.inventory.EquipmentSlotType.*;

public class TBClientEventHandler {

    @SubscribeEvent
    public void setRotationAngles(HUSetRotationAnglesEvent event) {
        if (Suit.getSuit(event.getPlayer()) == Suit.SUITS.get(new ResourceLocation(TheBoys.MODID, "atrain"))
                || Suit.getSuit(event.getPlayer()) == Suit.SUITS.get(new ResourceLocation(TheBoys.MODID, "homelander"))) {
            if (event.getPlayer().getItemStackFromSlot(CHEST).getItem() instanceof SuitItem) {
                event.getPlayerModel().bipedBodyWear.showModel = false;
                event.getPlayerModel().bipedRightArmwear.showModel = false;
                event.getPlayerModel().bipedLeftArmwear.showModel = false;
            }

            if (event.getPlayer().getItemStackFromSlot(FEET).getItem() instanceof SuitItem
                    || event.getPlayer().getItemStackFromSlot(LEGS).getItem() instanceof SuitItem) {
                event.getPlayerModel().bipedRightLegwear.showModel = false;
                event.getPlayerModel().bipedLeftLegwear.showModel = false;
            }
        }
    }
}