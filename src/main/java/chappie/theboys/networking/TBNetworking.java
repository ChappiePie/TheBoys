package chappie.theboys.networking;

import chappie.theboys.TheBoys;
import chappie.theboys.networking.client.ClientSpawnTrail;
import chappie.theboys.networking.server.ServerSetEyeOptions;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class TBNetworking {

    public static void registerClientMessages() {
        ClientPlayNetworking.registerGlobalReceiver(ClientSpawnTrail.PACKET, ClientSpawnTrail::handle);

        TheBoys.LOGGER.debug("Registered client network");
    }

    public static void registerMessages() {
        ServerPlayNetworking.registerGlobalReceiver(ServerSetEyeOptions.PACKET, ServerSetEyeOptions::handle);
        TheBoys.LOGGER.debug("Registered server network");
    }
}