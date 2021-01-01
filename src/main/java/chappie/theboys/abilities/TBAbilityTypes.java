package chappie.theboys.abilities;

import chappie.theboys.TheBoys;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.heroesunited.heroesunited.common.abilities.AbilityType;

@Mod.EventBusSubscriber(modid = TheBoys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TBAbilityTypes {
    public static final AbilityType SPEED = new AbilityType(SpeedAbility::new, TheBoys.MODID, "speed");
    public static final AbilityType LASERS_FROM_EYES = new AbilityType(LasersFromEyesAbility::new, TheBoys.MODID, "lasers_from_eyes");
    public static final AbilityType FIRE_IMMUNITY = new AbilityType(FireImmunityAbility::new, TheBoys.MODID, "fire_immunitry");

    @SubscribeEvent
    public static void registerAbilities(RegistryEvent.Register<AbilityType> e) {
        e.getRegistry().register(SPEED);
        e.getRegistry().register(FIRE_IMMUNITY);
        e.getRegistry().register(LASERS_FROM_EYES);
    }
}
