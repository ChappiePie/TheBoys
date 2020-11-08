package chappie.theboys.abilities.suits;

import chappie.theboys.TheBoys;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.heroesunited.heroesunited.common.abilities.suit.*;

@Mod.EventBusSubscriber(modid = TheBoys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TBSuitTypes {
    public static final SuitType ATRAIN = new SuitType(ATrainSuit::new).setRegistryName(TheBoys.MODID, "atrain");
    public static final SuitType HOMELANDER = new SuitType(HomelanderSuit::new).setRegistryName(TheBoys.MODID, "homelander");

    @SubscribeEvent
    public static void registerSuits(RegistryEvent.Register<SuitType> e) {
        e.getRegistry().register(ATRAIN);
        e.getRegistry().register(HOMELANDER);
    }
}
