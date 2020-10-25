package chappie.theboys.abilities;

import chappie.theboys.TheBoys;
import com.google.common.collect.Lists;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.heroesunited.generatorrex.abilities.superpower.Superpower;

@Mod.EventBusSubscriber(modid = TheBoys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TBSuperpowers {
    public static final Superpower ATRAIN = new Superpower(() ->  Lists.newArrayList(TBAbilityTypes.ATRAIN));
    public static final Superpower HOMELANDER = new Superpower(() -> Lists.newArrayList(TBAbilityTypes.HOMELANDER));

    @SubscribeEvent
    public static void registerSuperpowers(RegistryEvent.Register<Superpower> e) {
        e.getRegistry().register(TBSuperpowers.ATRAIN.setRegistryName(TheBoys.MODID, "atrain"));
        e.getRegistry().register(TBSuperpowers.HOMELANDER.setRegistryName(TheBoys.MODID, "homelander"));
    }
}
