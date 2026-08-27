package chappie.theboys.networking;

import chappie.theboys.TheBoys;
import chappie.theboys.networking.client.ClientSpawnTrail;
import chappie.theboys.networking.server.ServerSetEyeOptions;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class TBNetworking {

    public static void registerClientMessages() {
        PayloadTypeRegistry.clientboundPlay().register(ClientSpawnTrail.PACKET, ClientSpawnTrail.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(ClientSpawnTrail.PACKET, (packet, context) -> packet.handle(context.player(), context.responseSender()));

        TheBoys.LOGGER.debug("Registered client network");
    }

    public static void registerMessages() {
        PayloadTypeRegistry.serverboundPlay().register(ServerSetEyeOptions.PACKET, ServerSetEyeOptions.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ServerSetEyeOptions.PACKET, (packet, context) -> packet.handle(context.player(), context.responseSender()));

        TheBoys.LOGGER.debug("Registered server network");
    }
}