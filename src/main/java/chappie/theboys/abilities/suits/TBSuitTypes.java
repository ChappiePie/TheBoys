package chappie.theboys.abilities.suits;

import chappie.theboys.TheBoys;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.heroesunited.generatorrex.abilities.suit.SuitType;

@Mod.EventBusSubscriber(modid = TheBoys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TBSuitTypes {
    public static final SuitType ATRAIN = new SuitType(ATrainSuit::new);
    public static final SuitType HOMELANDER = new SuitType(HomelanderSuit::new);

    @SubscribeEvent
    public static void registerSuits(RegistryEvent.Register<SuitType> e) {
        e.getRegistry().register(ATRAIN.setRegistryName(TheBoys.MODID, "atrain"));
        e.getRegistry().register(HOMELANDER.setRegistryName(TheBoys.MODID, "homelander"));
    }
}
