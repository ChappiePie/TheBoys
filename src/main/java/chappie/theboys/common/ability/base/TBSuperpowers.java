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
import chappie.theboys.common.ability.interfaces.IHasOverlay;
import chappie.theboys.util.TBCommonUtil;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.awt.*;

public class TBSuperpowers {
    public static final Superpower HOMELANDER = register("homelander", new TBSuperpower(
            HeatVisionAbility.of("lasers", k -> k.keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.HELD),
                            k -> k.keyType(KeyMap.KeyType.MOUSE_RIGHT).action(KeyCondition.Action.HELD))
                    .change(TBCommonUtil.COLOR, Color.RED)
                    .additionalData(a ->
                            new IHasOverlay(a, (b) -> b.uOffset(48)
                                    .keyType(() -> !a.conditionManager.test("eyes") ?
                                            KeyMap.KeyType.FIRST : KeyMap.KeyType.MOUSE_RIGHT))
                    ),
            AbilityBuilder.of("flight", TBAbilityTypes.FLIGHT)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.SECOND).action(KeyCondition.Action.TOGGLE), "enabling", "slow_falling")
                    //.condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.JUMP).action(KeyCondition.Action.ACTION), "boost")
                    .condition(a -> new DoubleJumpCondition(a).shouldStop(() -> a.entity.onGround() || a.entity.isShiftKeyDown()), "enabling")
                    .additionalData(a ->
                            new IHasOverlay(a, (b) -> b.uOffset(32)
                                    .keyType(() -> !a.conditionManager.test("slow_falling") ?
                                            KeyMap.KeyType.SECOND : KeyMap.KeyType.JUMP))
                    ),
            AbilityBuilder.of("super_hearing", TBAbilityTypes.SUPER_HEARING)
                    .additionalData(a -> new IHasOverlay(a, (b) -> b.uOffset(0)))
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.THIRD).action(KeyCondition.Action.HELD), "enabling"),
            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(2.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(10.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("jump_boost", b -> b.attribute(ModRegistries.JUMP_BOOST).amount(1.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("fall_resistance", b -> b.attribute(ModRegistries.FALL_RESISTANCE).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADDITION)),
            DamageImmunityAbility.of("fire_immunity", "lava", "onFire", "inFire", "hotFloor"))
            .uOffset(16));

    public static final Superpower A_TRAIN = register("a_train", new TBSuperpower(
            AbilityBuilder.of("speed", TBAbilityTypes.SPEED)
                    .change(TBCommonUtil.COLOR, Color.BLUE)
                    .additionalData(a -> new IHasOverlay(a, (b) -> b.uOffset(0)))
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.TOGGLE), "enabling"),
            AbilityBuilder.of("focus", TBAbilityTypes.FOCUS_ON_GOAL)
                    .additionalData(a -> new IHasOverlay(a, (b) -> b.uOffset(0)))
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.SECOND).action(KeyCondition.Action.TOGGLE), "enabling"),
            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(1.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(1.0D).operation(AttributeModifier.Operation.ADDITION))
    ).uOffset(32));

    public static final Superpower TRANSLUCENT = register("transclucent", new TBSuperpower(
            AbilityBuilder.of("translucent", TBAbilityTypes.TRANSLUCENT)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.HELD), "enabling"),
            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(1.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(5.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("fall_resistance", b -> b.attribute(ModRegistries.FALL_RESISTANCE).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADDITION))
    ));

    public static final Superpower STARLIGHT = register("starlight", new TBSuperpower(
            AbilityBuilder.of("lasers", TBAbilityTypes.GLOW_EYES).condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.HELD)).change(TBCommonUtil.COLOR, Color.YELLOW),
            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(1.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(5.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("jump_boost", b -> b.attribute(ModRegistries.JUMP_BOOST).amount(1.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("fall_resistance", b -> b.attribute(ModRegistries.FALL_RESISTANCE).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADDITION))
    ));

    public static final Superpower TEST = register("test", new TBSuperpower(
            AbilityBuilder.of("speed", TBAbilityTypes.SPEED).change(TBCommonUtil.COLOR, Color.RED)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.TOGGLE), "enabling"),
            AbilityBuilder.of("lasers", TBAbilityTypes.HEAT_VISION).change(TBCommonUtil.COLOR, Color.BLUE)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.SECOND).action(KeyCondition.Action.HELD), "enabling"),

            AbilityBuilder.of("bruh", TBAbilityTypes.BRUH)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.THIRD)
                            .action(KeyCondition.Action.HELD), "enabling")
    ));

    public static <T extends TBSuperpower> T register(String id, T item) {
        return Registry.register(Superpower.REGISTRY, TheBoys.id(id), item);
    }

    public static void init() {

    }
}
