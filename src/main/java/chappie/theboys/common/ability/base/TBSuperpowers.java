package chappie.theboys.common.ability.base;

import chappie.modulus.Modulus;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.common.ability.base.condition.AbilityEnabledCondition;
import chappie.modulus.common.ability.base.condition.KeyCondition;
import chappie.modulus.util.KeyMap;
import chappie.modulus.util.ModAttributes;
import chappie.theboys.TheBoys;
import chappie.theboys.common.ability.AttributeModifierAbility;
import chappie.theboys.common.ability.DamageImmunityAbility;
import chappie.theboys.common.ability.HeatVisionAbility;
import chappie.theboys.util.TBCommonUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.awt.*;

public class TBSuperpowers {
    public static final DeferredRegister<Superpower> SUPERPOWERS = DeferredRegister.create(new ResourceLocation(Modulus.MODID, "superpowers"), TheBoys.MODID);

    public static final RegistryObject<Superpower> HOMELANDER = SUPERPOWERS.register("homelander", () -> new Superpower(
            HeatVisionAbility.of("lasers", k -> k.keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.HELD),
                    k -> k.keyType(KeyMap.KeyType.MOUSE_RIGHT).action(KeyCondition.Action.HELD)
            ).change(TBCommonUtil.COLOR, Color.RED),
            AbilityBuilder.of("flight", TBAbilityTypes.FLIGHT.get())
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.SECOND).action(KeyCondition.Action.TOGGLE), "enabling")
                    //.condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.JUMP).action(KeyCondition.Action.ACTION), "boost")
                    .condition(a -> new DoubleJumpCondition(a).shouldStop(() -> a.entity.isOnGround() || a.entity.isShiftKeyDown()), "enabling"),
            AbilityBuilder.of("super_hearing", TBAbilityTypes.SUPER_HEARING.get())
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.THIRD).action(KeyCondition.Action.HELD), "enabling"),
            AttributeModifierAbility.of("gravity", b -> b.attribute(ForgeMod.ENTITY_GRAVITY.get()).amount(-1.0D).operation(AttributeModifier.Operation.MULTIPLY_TOTAL)).condition(a -> new AbilityEnabledCondition(a).abilityName("flight"), "enabling"),
            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(2.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(10.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("jump_boost", b -> b.attribute(ModAttributes.JUMP_BOOST.get()).amount(2.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("fall_resistance", b -> b.attribute(ModAttributes.FALL_RESISTANCE.get()).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADDITION)),
            DamageImmunityAbility.of("fire_immunity", "lava", "onFire", "inFire", "hotFloor"))
    );

    public static final RegistryObject<Superpower> A_TRAIN = SUPERPOWERS.register("a_train", () -> new Superpower(
            AbilityBuilder.of("speed", TBAbilityTypes.SPEED.get()).change(TBCommonUtil.COLOR, Color.BLUE)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.TOGGLE), "enabling"),
            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(1.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(1.0D).operation(AttributeModifier.Operation.ADDITION))
    ));

    public static final RegistryObject<Superpower> TRANSLUCENT = SUPERPOWERS.register("transclucent", () -> new Superpower(
            AbilityBuilder.of("translucent", TBAbilityTypes.TRANSLUCENT.get())
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.HELD), "enabling"),
            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(1.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(5.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("fall_resistance", b -> b.attribute(ModAttributes.FALL_RESISTANCE.get()).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADDITION))
    ));

    public static final RegistryObject<Superpower> STARLIGHT = SUPERPOWERS.register("starlight", () -> new Superpower(
            AbilityBuilder.of("lasers", TBAbilityTypes.GLOW_EYES.get()).condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.HELD)).change(TBCommonUtil.COLOR, Color.YELLOW),
            AttributeModifierAbility.of("gravity", b -> b.attribute(ForgeMod.ENTITY_GRAVITY.get()).amount(-1.0D).operation(AttributeModifier.Operation.MULTIPLY_TOTAL)).condition(a -> new AbilityEnabledCondition(a).abilityName("flight"), "enabling"),
            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(1.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(5.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("jump_boost", b -> b.attribute(ModAttributes.JUMP_BOOST.get()).amount(2.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("fall_resistance", b -> b.attribute(ModAttributes.FALL_RESISTANCE.get()).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADDITION))
    ));

    public static final RegistryObject<Superpower> TEST = SUPERPOWERS.register("test", () -> new Superpower(
            AbilityBuilder.of("speed", TBAbilityTypes.SPEED.get()).change(TBCommonUtil.COLOR, Color.RED)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.TOGGLE), "enabling"),
            AbilityBuilder.of("lasers", TBAbilityTypes.HEAT_VISION.get()).change(TBCommonUtil.COLOR, Color.BLUE)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.SECOND).action(KeyCondition.Action.HELD), "enabling"),

            AbilityBuilder.of("bruh", TBAbilityTypes.BRUH.get())
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.THIRD)
                            .action(KeyCondition.Action.ACTION), "enabling")
    ));
}
