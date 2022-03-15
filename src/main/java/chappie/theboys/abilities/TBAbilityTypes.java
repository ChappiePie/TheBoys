package chappie.theboys.abilities;

import chappie.theboys.TheBoys;
import net.minecraftforge.registries.DeferredRegister;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;
import xyz.heroesunited.heroesunited.common.abilities.AbilityType;
import xyz.heroesunited.heroesunited.common.abilities.JSONAbility;

@SuppressWarnings("unused")
public class TBAbilityTypes {
    
    public static final DeferredRegister<AbilityType> ABILITIES = DeferredRegister.create(AbilityType.class, TheBoys.MODID);

    public static final AbilityType SPEED = register("speed", SpeedAbility::new);
    public static final AbilityType LIGHTNING_FROM_ARMS = register("lightning_from_arms", LightningFromArmsAbility::new);
    public static final AbilityType JERK_OFF = register("jerk_off", JerkOffAbility::new);
    public static final AbilityType SUPER_LIFT = register("super_lift", SuperLiftAbility::new);
    public static final AbilityType OVERLAY = register("overlay", OverlayAbility::new);
    public static final AbilityType TB_ATTRIBUTE = register("attribute_modifier", TBAttributeModifierAbility::new);
    public static final AbilityType SUPER_HEARING = register("super_hearing", JSONAbility::new);
    public static final AbilityType STARLIGHT = register("starlight", StarLightAbility::new);
    public static final AbilityType LIGHTING_LENGTH_CHANGE = register("lightning_length_change", (abilityType, player, jsonObject) -> new ScrollAbility(abilityType, player, jsonObject, (a, delta) -> {
        for (LightningFromArmsAbility ability : AbilityHelper.getListOfType(LightningFromArmsAbility.class, AbilityHelper.getAbilities(player))) {
            if (a.getAdditionalData().equals(ability.getAdditionalData()) && ability.getEnabled()) {
                ability.changeDistance(delta);
                break;
            }
        }
    }));

    private static AbilityType register(String name, AbilityType.AbilitySupplier ability) {
        AbilityType type = new AbilityType(ability);
        ABILITIES.register(name, () -> type);
        return type;
    }
}
