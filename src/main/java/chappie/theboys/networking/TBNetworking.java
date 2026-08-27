package chappie.theboys.networking;

import chappie.theboys.TheBoys;
import chappie.theboys.networking.client.ClientSpawnTrail;
import chappie.theboys.networking.packet.SyncTheBoysCapPacket;
import chappie.theboys.networking.server.ServerSetEyeOptions;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class TBNetworking {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(TBNetworking::onRegisterPayloadHandler);
    }

    private static void onRegisterPayloadHandler(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(TheBoys.MODID);

        if (FMLLoader.getDist().isClient()) {
            // On client, register with proper client handlers (in a separate class to isolate client dependencies)
            registerClientHandlers(registrar);
        } else {
            // On dedicated server, register codecs only with no-op handlers
            registrar.playToClient(SyncTheBoysCapPacket.PACKET, SyncTheBoysCapPacket.CODEC, (packet, context) -> {});
            registrar.playToClient(ClientSpawnTrail.PACKET, ClientSpawnTrail.CODEC, (packet, context) -> {});
        }

        // Server-bound packets
        registrar.playToServer(
                ServerSetEyeOptions.PACKET,
                ServerSetEyeOptions.CODEC,
                (packet, context) -> context.enqueueWork(() ->
                        packet.handle((ServerPlayer) context.player())
                )
        );
    }

    /**
     * Only called on CLIENT dist. Delegates to ClientPayloadHandler which imports client-only classes.
     */
    private static void registerClientHandlers(PayloadRegistrar registrar) {
        chappie.theboys.networking.client.ClientPayloadHandler.registerAll(registrar);
    }

    public static void send(CustomPacketPayload packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToTrackingEntityAndSelf(CustomPacketPayload packet, Entity entityToTrack) {
        PacketDistributor.sendToPlayersTrackingEntity(entityToTrack, packet);
        if (entityToTrack instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, packet);
        }
    }
}
