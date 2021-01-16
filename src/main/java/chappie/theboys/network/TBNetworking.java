package chappie.theboys.network;

import chappie.theboys.TheBoys;
import chappie.theboys.network.client.ClientSetCompoundV;
import chappie.theboys.network.client.ClientSetSlowMotion;
import chappie.theboys.network.server.ServerSetSlowMotion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class TBNetworking {

    public static SimpleChannel INSTANCE;
    private static int id = 0;

    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(TheBoys.MODID, "tbnetworking"), () -> "1.0", s -> true, s -> true);
        INSTANCE.registerMessage(id++, ClientSetCompoundV.class, ClientSetCompoundV::toBytes, ClientSetCompoundV::new, ClientSetCompoundV::handle);
        INSTANCE.registerMessage(id++, ClientSetSlowMotion.class, ClientSetSlowMotion::toBytes, ClientSetSlowMotion::new, ClientSetSlowMotion::handle);
        INSTANCE.registerMessage(id++, ServerSetSlowMotion.class, ServerSetSlowMotion::toBytes, ServerSetSlowMotion::new, ServerSetSlowMotion::handle);
    }
}
