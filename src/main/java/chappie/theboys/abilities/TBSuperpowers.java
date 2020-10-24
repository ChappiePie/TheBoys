package chappie.theboys.abilities;

import chappie.theboys.TheBoys;
import com.google.common.collect.Lists;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import xyz.heroesunited.generatorrex.abilities.AbilityType;
import xyz.heroesunited.generatorrex.abilities.superpower.Superpower;

import java.util.List;

@Mod.EventBusSubscriber(modid = TheBoys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TBSuperpowers {
    public static final Superpower ATRAIN = new Superpower(() -> {
        List<AbilityType> list = Lists.newArrayList();
        list.add(TBAbilityTypes.ATRAIN);
        return list;
    });

    public static final Superpower HOMELANDER = new Superpower(() -> {
        List<AbilityType> list = Lists.newArrayList();
        list.add(TBAbilityTypes.HOMELANDER);
        return list;
    });

    @SubscribeEvent
    public static void registerSuperpowers(RegistryEvent.Register<Superpower> e) {
        e.getRegistry().register(TBSuperpowers.ATRAIN.setRegistryName(TheBoys.MODID, "atrain"));
        e.getRegistry().register(TBSuperpowers.HOMELANDER.setRegistryName(TheBoys.MODID, "homelander"));
    }
}
