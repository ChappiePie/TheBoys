package chappie.theboys.abilities;

import chappie.theboys.TheBoys;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.heroesunited.heroesunited.common.abilities.AbilityType;

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
