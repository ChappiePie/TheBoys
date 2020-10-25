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

@Mod.EventBusSubscriber(modid = TheBoys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TBAbilityTypes {
    public static final AbilityType ATRAIN = new AbilityType(ATrainAbility::new, TheBoys.MODID, "atrain");
    public static final AbilityType HOMELANDER = new AbilityType(HomelanderAbility::new, TheBoys.MODID, "homelander");

    @SubscribeEvent
    public static void registerAbilities(RegistryEvent.Register<AbilityType> e) {
        e.getRegistry().register(ATRAIN);
        e.getRegistry().register(HOMELANDER);
    }
}
