package chappie.theboys.networking;

import chappie.theboys.TheBoys;
import chappie.theboys.networking.client.ClientSyncTheBoysCap;
import chappie.theboys.networking.server.ServerSetEyeOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class TBNetworking {

    public static SimpleChannel INSTANCE;
    private static int id = -1;

    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(TheBoys.MODID, "networking"), () -> "1.0", s -> true, s -> true);
        INSTANCE.registerMessage(id++, ClientSyncTheBoysCap.class, ClientSyncTheBoysCap::toBytes, ClientSyncTheBoysCap::new, ClientSyncTheBoysCap::handle);
        INSTANCE.registerMessage(id++, ServerSetEyeOptions.class, ServerSetEyeOptions::toBytes, ServerSetEyeOptions::new, ServerSetEyeOptions::handle);
    }
}