package chappie.theboys.abilities;

import chappie.theboys.TheBoys;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.heroesunited.heroesunited.common.abilities.AbilityType;

@Mod.EventBusSubscriber(modid = TheBoys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TBAbilityTypes {
    public static final AbilityType SPEED = new AbilityType(SpeedAbility::new, TheBoys.MODID, "speed");
    public static final AbilityType LIGHTNING_FROM_ARMS = new AbilityType(LightningFromArmsAbility::new, TheBoys.MODID, "lightning_from_arms");
    public static final AbilityType JERK_OFF = new AbilityType(JerkOffAbility::new, TheBoys.MODID, "jerk_off");
    public static final AbilityType SUPER_HEARING = new AbilityType(SuperHearingAbility::new, TheBoys.MODID, "super_hearing");
    public static final AbilityType STARLIGHT = new AbilityType(StarLightAbility::new, TheBoys.MODID, "starlight");

    @SubscribeEvent
    public static void registerAbilities(RegistryEvent.Register<AbilityType> e) {
        e.getRegistry().register(SPEED);
        e.getRegistry().register(LIGHTNING_FROM_ARMS);
        e.getRegistry().register(JERK_OFF);
        e.getRegistry().register(SUPER_HEARING);
        e.getRegistry().register(STARLIGHT);
    }
}
