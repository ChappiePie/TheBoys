package chappie.theboys.common.ability.base;

import chappie.modulus.Modulus;
import chappie.modulus.common.ability.base.AbilityType;
import chappie.theboys.TheBoys;
import chappie.theboys.common.ability.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class TBAbilityTypes {
    public static final DeferredRegister<AbilityType> ABILITIES = DeferredRegister.create(new ResourceLocation(Modulus.MODID, "ability_types"), TheBoys.MODID);

    public static final RegistryObject<AbilityType> GLOW_EYES = ABILITIES.register("glow_eyes", () -> new AbilityType(GlowEyesAbility::new));
    public static final RegistryObject<AbilityType> HEAT_VISION = ABILITIES.register("heat_vision", () -> new AbilityType(HeatVisionAbility::new));
    public static final RegistryObject<AbilityType> SPEED = ABILITIES.register("speed", () -> new AbilityType(SpeedAbility::new));
    public static final RegistryObject<AbilityType> FLIGHT = ABILITIES.register("flight", () -> new AbilityType(FlightAbility::new));
    public static final RegistryObject<AbilityType> SUPER_HEARING = ABILITIES.register("super_hearing", () -> new AbilityType(SuperHearingAbility::new));
    public static final RegistryObject<AbilityType> ATTRIBUTE_MODIFIER = ABILITIES.register("attribute_modifier", () -> new AbilityType(AttributeModifierAbility::new));
    public static final RegistryObject<AbilityType> DAMAGE_IMMUNITY = ABILITIES.register("damage_immunity", () -> new AbilityType(DamageImmunityAbility::new));
    public static final RegistryObject<AbilityType> TRANSLUCENT = ABILITIES.register("translucent", () -> new AbilityType(TranslucentAbility::new));

    public static final RegistryObject<AbilityType> BRUH = ABILITIES.register("bruh", () -> new AbilityType(BruhAbility::new));
}
