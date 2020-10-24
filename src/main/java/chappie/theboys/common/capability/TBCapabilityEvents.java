package chappie.theboys.common.capability;

import chappie.theboys.TheBoys;
import chappie.theboys.network.SetSpeedLevelMessage;
import chappie.theboys.network.TBNetworking;
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
            event.addCapability(new ResourceLocation(TheBoys.MODID, "boyscap"), new BoysProvider((PlayerEntity) event.getObject()));
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        IBoys newCap = BoysCap.getCap(event.getPlayer());
        IBoys oldCap = BoysCap.getCap(event.getOriginal());
        syncDeath(newCap, oldCap);
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking e) {
        e.getTarget().getCapability(BoysCap.CAPABILITY).ifPresent(a -> {
            if (e.getPlayer() instanceof ServerPlayerEntity) {
                syncServerMessages(e.getEntity(), a);
            }
        });
    }

    @SubscribeEvent
    public static void onJoinWorld(EntityJoinWorldEvent e) {
        e.getEntity().getCapability(BoysCap.CAPABILITY).ifPresent(a -> {
            if (e.getEntity() instanceof ServerPlayerEntity) {
                syncServerMessages(e.getEntity(), a);
            }
        });
    }

    public static void syncDeath(IBoys newCap, IBoys oldCap) {
        newCap.setSpeedLevel(0);
        newCap.setInSpeed(false);
    }

    public static void syncServerMessages(Entity entity, IBoys cap) {
        TBNetworking.INSTANCE.sendTo(new SetSpeedLevelMessage(entity.getEntityId(), cap.getSpeedLevel()), ((ServerPlayerEntity) entity).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    }
}
