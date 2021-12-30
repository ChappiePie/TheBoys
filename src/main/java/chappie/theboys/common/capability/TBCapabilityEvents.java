package chappie.theboys.common.capability;

import chappie.theboys.TheBoys;
import chappie.theboys.network.TBNetworking;
import chappie.theboys.network.client.ClientSyncBoysCap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = TheBoys.MODID)
public class TBCapabilityEvents {

    @SubscribeEvent
    public static void attachCap(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(TheBoys.MODID, "theboys"), new BoysProvider((Player) event.getObject()));
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().reviveCaps();
            IBoys newCap = BoysCap.getCap(event.getPlayer());
            IBoys oldCap = BoysCap.getCap(event.getOriginal());
            newCap.deserializeNBT(oldCap.serializeNBT());
            newCap.sync();
            event.getOriginal().invalidateCaps();
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking e) {
        if (e.getPlayer() instanceof ServerPlayer) {
            e.getTarget().getCapability(BoysCap.CAPABILITY).ifPresent(a -> {
                TBNetworking.INSTANCE.sendTo(new ClientSyncBoysCap(e.getTarget().getId(), a.serializeNBT()), ((ServerPlayer) e.getPlayer()).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
            });
        }
    }

    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent e) {
        if (e.getEntity() instanceof ServerPlayer) {
            e.getEntity().getCapability(BoysCap.CAPABILITY).ifPresent(a -> a.syncToAll());
        }
    }
}
