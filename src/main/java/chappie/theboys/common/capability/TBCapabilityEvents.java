package chappie.theboys.common.capability;

import chappie.theboys.TheBoys;
import chappie.theboys.network.TBNetworking;
import chappie.theboys.network.client.ClientSyncBoysCap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = TheBoys.MODID)
public class TBCapabilityEvents {

    @SubscribeEvent
    public static void attachCap(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            event.addCapability(new ResourceLocation(TheBoys.MODID, "theboys"), new BoysProvider((PlayerEntity) event.getObject()));
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        IBoys newCap = BoysCap.getCap(event.getPlayer());
        IBoys oldCap = BoysCap.getCap(event.getOriginal());
        newCap.deserializeNBT(oldCap.serializeNBT());
        newCap.setCompoundV(oldCap.haveCompoundV());
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking e) {
        if (e.getPlayer() instanceof ServerPlayerEntity) {
            e.getTarget().getCapability(BoysCap.CAPABILITY).ifPresent(a -> {
                TBNetworking.INSTANCE.sendTo(new ClientSyncBoysCap(e.getTarget().getId(), a.serializeNBT()), ((ServerPlayerEntity) e.getPlayer()).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
            });
        }
    }

    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent e) {
        if (e.getEntity() instanceof ServerPlayerEntity) {
            e.getEntity().getCapability(BoysCap.CAPABILITY).ifPresent(a -> a.syncToAll());
        }
    }
}
