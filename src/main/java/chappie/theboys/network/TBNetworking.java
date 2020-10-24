package chappie.theboys.network;

import chappie.theboys.TheBoys;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class TBNetworking {

    public static SimpleChannel INSTANCE;
    private static int id = 0;

    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(TheBoys.MODID, "tbnetworking"), () -> "1.0", s -> true, s -> true);
        INSTANCE.registerMessage(id++, SetSpeedLevelMessage.class, SetSpeedLevelMessage::toBytes, SetSpeedLevelMessage::new, SetSpeedLevelMessage::handle);
        INSTANCE.registerMessage(id++, SetInSpeedMessage.class, SetInSpeedMessage::toBytes, SetInSpeedMessage::new, SetInSpeedMessage::handle);
    }
}
