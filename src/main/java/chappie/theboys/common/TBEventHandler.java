package chappie.theboys.common;

import chappie.theboys.common.capability.BoysCap;
import chappie.theboys.util.TBUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TBEventHandler {

    @SubscribeEvent
    public void playerTick(PlayerEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            player.getCapability(BoysCap.CAPABILITY).ifPresent(a -> {
            });
        }
    }
}