package chappie.theboys.common.ability.base;

import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.common.ability.base.condition.KeyCondition;
import chappie.modulus.util.KeyMap;
import chappie.modulus.util.ModRegistries;
import chappie.theboys.TheBoys;
import chappie.theboys.common.ability.AttributeModifierAbility;
import chappie.theboys.common.ability.DamageImmunityAbility;
import chappie.theboys.common.ability.HeatVisionAbility;
import chappie.theboys.util.TBCommonUtil;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.awt.*;

// @TODO gravity

public class TBSuperpowers {
    public static final Superpower HOMELANDER = register("homelander", new Superpower(
            HeatVisionAbility.of("lasers", k -> k.keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.HELD),
                    k -> k.keyType(KeyMap.KeyType.MOUSE_RIGHT).action(KeyCondition.Action.HELD)
            ).change(TBCommonUtil.COLOR, Color.RED),
            AbilityBuilder.of("flight", TBAbilityTypes.FLIGHT)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.SECOND).action(KeyCondition.Action.TOGGLE), "enabling")
                    //.condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.JUMP).action(KeyCondition.Action.ACTION), "boost")
                    .condition(a -> new DoubleJumpCondition(a).shouldStop(() -> a.entity.onGround() || a.entity.isShiftKeyDown()), "enabling"),
            AbilityBuilder.of("super_hearing", TBAbilityTypes.SUPER_HEARING)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.THIRD).action(KeyCondition.Action.HELD), "enabling"),
            //AttributeModifierAbility.of("gravity", b -> b.attribute(ForgeMod.ENTITY_GRAVITY).amount(-1.0D).operation(AttributeModifier.Operation.MULTIPLY_TOTAL)).condition(a -> new AbilityEnabledCondition(a).abilityName("flight"), "enabling"),
            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(2.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(10.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("jump_boost", b -> b.attribute(ModRegistries.JUMP_BOOST).amount(2.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("fall_resistance", b -> b.attribute(ModRegistries.FALL_RESISTANCE).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADDITION)),
            DamageImmunityAbility.of("fire_immunity", "lava", "onFire", "inFire", "hotFloor"))
    );

    public static final Superpower A_TRAIN = register("a_train", new Superpower(
            AbilityBuilder.of("speed", TBAbilityTypes.SPEED).change(TBCommonUtil.COLOR, Color.BLUE)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.TOGGLE), "enabling"),
            AbilityBuilder.of("focus", TBAbilityTypes.FOCUS_ON_GOAL)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.SECOND).action(KeyCondition.Action.TOGGLE), "enabling"),
            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(1.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(1.0D).operation(AttributeModifier.Operation.ADDITION))
    ));

    public static final Superpower TRANSLUCENT = register("transclucent", new Superpower(
            AbilityBuilder.of("translucent", TBAbilityTypes.TRANSLUCENT)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.HELD), "enabling"),
            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(1.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(5.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("fall_resistance", b -> b.attribute(ModRegistries.FALL_RESISTANCE).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADDITION))
    ));

    public static final Superpower STARLIGHT = register("starlight", new Superpower(
            AbilityBuilder.of("lasers", TBAbilityTypes.GLOW_EYES).condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.HELD)).change(TBCommonUtil.COLOR, Color.YELLOW),
            //AttributeModifierAbility.of("gravity", b -> b.attribute(ForgeMod.ENTITY_GRAVITY).amount(-1.0D).operation(AttributeModifier.Operation.MULTIPLY_TOTAL)).condition(a -> new AbilityEnabledCondition(a).abilityName("flight"), "enabling"),
            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(1.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(5.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("jump_boost", b -> b.attribute(ModRegistries.JUMP_BOOST).amount(2.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("fall_resistance", b -> b.attribute(ModRegistries.FALL_RESISTANCE).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADDITION))
    ));

    public static final Superpower TEST = register("test", new Superpower(
            AbilityBuilder.of("speed", TBAbilityTypes.SPEED).change(TBCommonUtil.COLOR, Color.RED)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.TOGGLE), "enabling"),
            AbilityBuilder.of("lasers", TBAbilityTypes.HEAT_VISION).change(TBCommonUtil.COLOR, Color.BLUE)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.SECOND).action(KeyCondition.Action.HELD), "enabling"),

            AbilityBuilder.of("bruh", TBAbilityTypes.BRUH)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.THIRD)
                            .action(KeyCondition.Action.HELD), "enabling")
    ));

    public static <T extends Superpower> T register(String id, T item) {
        return Registry.register(Superpower.REGISTRY, TheBoys.id(id), item);
    }

    public static void init() {

    }
}
