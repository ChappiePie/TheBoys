package chappie.theboys.network;

import chappie.theboys.TheBoys;
import chappie.theboys.network.client.ClientSyncBoysCap;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class TBNetworking {

    public static SimpleChannel INSTANCE;
    private static int id = 0;

    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(TheBoys.MODID, "tbnetworking"), () -> "1.0", s -> true, s -> true);
        INSTANCE.registerMessage(id++, ClientSyncBoysCap.class, ClientSyncBoysCap::toBytes, ClientSyncBoysCap::new, ClientSyncBoysCap::handle);
    }
}
