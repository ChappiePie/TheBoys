package chappie.theboys.networking;

import chappie.theboys.TheBoys;
import chappie.theboys.networking.client.ClientSyncTheBoysCap;
import chappie.theboys.networking.server.ServerSetEyeOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class TBNetworking {

    private static final Marker MARKER = MarkerManager.getMarker("THEBOYS_NETWORK");
    public static SimpleChannel INSTANCE = ChannelBuilder.named(new ResourceLocation(TheBoys.MODID, "networking")).simpleChannel();

    public static void registerMessages() {
        INSTANCE.messageBuilder(ClientSyncTheBoysCap.class, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientSyncTheBoysCap::new)
                .encoder(ClientSyncTheBoysCap::toBytes)
                .consumerMainThread(ClientSyncTheBoysCap::handle)
                .add()

                .messageBuilder(ServerSetEyeOptions.class, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ServerSetEyeOptions::new)
                .encoder(ServerSetEyeOptions::toBytes)
                .consumerMainThread(ServerSetEyeOptions::handle)
                .add();
        TheBoys.LOGGER.debug(MARKER, "Registering Network {} v{}", INSTANCE.getName(), INSTANCE.getProtocolVersion());
    }
}