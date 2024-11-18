package chappie.theboys.common.ability.base;

import chappie.modulus.common.ability.base.AbilityType;
import chappie.theboys.TheBoys;
import chappie.theboys.common.ability.*;
import net.minecraft.core.Registry;

public class TBAbilityTypes {

    public static final AbilityType GLOW_EYES = register("glow_eyes", new AbilityType(GlowEyesAbility::new));
    public static final AbilityType HEAT_VISION = register("heat_vision", new AbilityType(HeatVisionAbility::new));
    public static final AbilityType SPEED = register("speed", new AbilityType(SpeedAbility::new));
    public static final AbilityType FOCUS_ON_GOAL = register("focus_on_goal", new AbilityType(FocusOnGoalAbility::new));
    public static final AbilityType FLIGHT = register("flight", new AbilityType(FlightAbility::new));
    public static final AbilityType SUPER_HEARING = register("super_hearing", new AbilityType(SuperHearingAbility::new));
    public static final AbilityType TRANSLUCENT = register("translucent", new AbilityType(TranslucentAbility::new));
    public static final AbilityType ENERGY_CHARGING = register("energy_charging", new AbilityType(EnergyChargingAbility::new));

    public static final AbilityType BRUH = register("bruh", new AbilityType(LightingAbility::new));

    public static <T extends AbilityType> T register(String id, T item) {
        return Registry.register(AbilityType.REGISTRY, TheBoys.id(id), item);
    }

    public static void init() {

    }
}
