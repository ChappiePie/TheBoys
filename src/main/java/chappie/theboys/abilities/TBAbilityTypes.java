package chappie.theboys.abilities;

import chappie.theboys.TheBoys;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.heroesunited.generatorrex.abilities.AbilityType;

@Mod.EventBusSubscriber(modid = TheBoys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TBAbilityTypes {
    public static final AbilityType ATRAIN = new AbilityType(ATrainAbility::new);
    public static final AbilityType HOMELANDER = new AbilityType(HomelanderAbility::new);

    @SubscribeEvent
    public static void registerAbilities(RegistryEvent.Register<AbilityType> e) {
        e.getRegistry().register(TBAbilityTypes.ATRAIN.setRegistryName(TheBoys.MODID, "atrain"));
        e.getRegistry().register(TBAbilityTypes.HOMELANDER.setRegistryName(TheBoys.MODID, "homelander"));
    }
}
