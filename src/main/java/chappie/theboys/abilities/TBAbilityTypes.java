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
    public static final AbilityType LIGHTNING_FROM_ARMS = new AbilityType(LightningFromArmsAbility::new, TheBoys.MODID, "lightning_from_arms");
    public static final AbilityType LIGHTNING_PROJECTILE = new AbilityType(LightningProjectileAbility::new, TheBoys.MODID, "lightning_projectile");
    public static final AbilityType JERK_OFF = new AbilityType(JerkOffAbility::new, TheBoys.MODID, "jerk_off");

    @SubscribeEvent
    public static void registerAbilities(RegistryEvent.Register<AbilityType> e) {
        e.getRegistry().register(SPEED);
        e.getRegistry().register(LASERS_FROM_EYES);
        e.getRegistry().register(LIGHTNING_FROM_ARMS);
        e.getRegistry().register(LIGHTNING_PROJECTILE);
        e.getRegistry().register(JERK_OFF);
    }
}
