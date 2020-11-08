package chappie.theboys.abilities;

import chappie.theboys.TheBoys;
import com.google.common.collect.Lists;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.heroesunited.heroesunited.common.abilities.superpower.Superpower;

@Mod.EventBusSubscriber(modid = TheBoys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TBSuperpowers {
    public static final Superpower ATRAIN = new Superpower(() ->  Lists.newArrayList(TBAbilityTypes.ATRAIN)).setRegistryName(TheBoys.MODID, "atrain");
    public static final Superpower HOMELANDER = new Superpower(() -> Lists.newArrayList(TBAbilityTypes.HOMELANDER)).setRegistryName(TheBoys.MODID, "homelander");

    @SubscribeEvent
    public static void registerSuperpowers(RegistryEvent.Register<Superpower> e) {
        e.getRegistry().register(ATRAIN);
        e.getRegistry().register(HOMELANDER);
    }
}
