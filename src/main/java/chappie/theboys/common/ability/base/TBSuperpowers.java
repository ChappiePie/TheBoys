package chappie.theboys.common.ability.base;

import chappie.modulus.common.ability.AttributeModifierAbility;
import chappie.modulus.common.ability.DamageImmunityAbility;
import chappie.modulus.common.ability.DamageResistanceAbility;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.AbilityType;
import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.common.ability.base.condition.DoubleKeyCondition;
import chappie.modulus.common.ability.base.condition.KeyCondition;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.KeyMap;
import chappie.modulus.util.ModRegistries;
import chappie.theboys.TheBoys;
import chappie.theboys.common.ability.FlightAbility;
import chappie.theboys.common.ability.FocusOnGoalAbility;
import chappie.theboys.common.ability.HeatVisionAbility;
import chappie.theboys.common.ability.interfaces.IHasOverlay;
import chappie.theboys.util.TBCommonUtil;
import net.minecraft.core.Registry;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.EntityHitResult;

import java.awt.*;
import java.util.List;

public class TBSuperpowers {
    public static final Superpower HOMELANDER = register("homelander", new TBSuperpower(
            HeatVisionAbility.of("lasers", k -> k.keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.HELD),
                            k -> k.keyType(KeyMap.KeyType.MOUSE_RIGHT).action(KeyCondition.Action.HELD))
                    .change(TBCommonUtil.COLOR, Color.RED)
                    .additionalData(a -> new IHasOverlay(a,
                            (b) -> b.uOffset(48).backgroundColor(() -> a.conditionManager.test("eyes") ? 0xFF637F : -1)
                                    .keyType(() -> !a.conditionManager.test("eyes") ?
                                            KeyMap.KeyType.FIRST : KeyMap.KeyType.MOUSE_RIGHT))
                    ),
            AbilityBuilder.of("flight", TBAbilityTypes.FLIGHT)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.SECOND).action(KeyCondition.Action.TOGGLE), "enabling", "pressed")
                    .condition(a -> new DoubleKeyCondition(a).shouldStop(() -> a.entity.onGround() || a.entity.isShiftKeyDown()), "enabling")
                    .condition(a -> new DoubleKeyCondition(a).keyType(KeyMap.KeyType.SPRINT).shouldStop(() -> !(a instanceof FlightAbility f && f.cooldown.end() && a.isEnabled() && a.dataManager.get(FlightAbility.SPRINTING))), "boost")
                    .additionalData(a ->
                            new IHasOverlay(a, (b) -> b.uOffset(32)
                                    .keyType(() -> !a.conditionManager.test("pressed") ?
                                            KeyMap.KeyType.SECOND : KeyMap.KeyType.JUMP))
                    ),
            AbilityBuilder.of("super_hearing", TBAbilityTypes.SUPER_HEARING)
                    .additionalData(a -> new IHasOverlay(a, (b) -> b.uOffset(64)))
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.THIRD).action(KeyCondition.Action.HELD), "enabling"),
            AbilityBuilder.of("xray", TBAbilityTypes.XRAY)
                    .additionalData(a -> new IHasOverlay(a, (b) -> b.uOffset(112)))
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FOURTH).action(KeyCondition.Action.HELD), "enabling"),

            AbilityBuilder.of("damage_resistance", AbilityType.DAMAGE_RESISTANCE).additionalData((a) -> a.dataManager.set(DamageResistanceAbility.AMPLIFIER, 8F)),
            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE.value()).amount(2.0D).operation(AttributeModifier.Operation.ADD_VALUE)),
            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH.value()).amount(10.0D).operation(AttributeModifier.Operation.ADD_VALUE)),
            AttributeModifierAbility.of("jump_boost", b -> b.attribute(ModRegistries.JUMP_BOOST.value()).amount(1.0D).operation(AttributeModifier.Operation.ADD_VALUE)),
            AttributeModifierAbility.of("fall_resistance", b -> b.attribute(ModRegistries.FALL_RESISTANCE.value()).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADD_VALUE)),
            DamageImmunityAbility.of("fire_immunity", List.of(DamageTypeTags.IS_FIRE)))
            .uOffset(16));

    public static final Superpower A_TRAIN = register("a_train", new TBSuperpower(
            AbilityBuilder.of("speed", TBAbilityTypes.SPEED)
                    .change(TBCommonUtil.COLOR, Color.BLUE)
                    .additionalData(a -> new IHasOverlay(a, (b) -> b.uOffset(80)))
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.TOGGLE), "enabling"),
            AbilityBuilder.of("focus", TBAbilityTypes.FOCUS_ON_GOAL)
                    .additionalData(a -> new IHasOverlay(a, (b) -> b.uOffset(96).backgroundColor(() -> {
                        if (a instanceof FocusOnGoalAbility a1 && !a1.hasSpeedAbility()) {
                            if (CommonUtil.pick(a.entity, 40) instanceof EntityHitResult hr && hr.getEntity() instanceof LivingEntity target) {
                                if (target.getEyePosition().distanceTo(a.entity.getEyePosition()) < 4) {
                                    return 8553091;
                                }
                            }
                        }
                        return -1;
                    })
                    ))
                    .condition(a -> new KeyCondition(a) {
                        @Override
                        public boolean get() {
                            if (a instanceof FocusOnGoalAbility a1) {
                                if (a1.condition(this, super.get())) {
                                    return true;
                                }
                            }
                            this.enabled = false;
                            return false;
                        }
                    }.keyType(KeyMap.KeyType.SECOND).action(KeyCondition.Action.TOGGLE), "enabling"),
            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE.value()).amount(1.0D).operation(AttributeModifier.Operation.ADD_VALUE)),
            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH.value()).amount(1.0D).operation(AttributeModifier.Operation.ADD_VALUE))
    ).uOffset(32));

    public static final Superpower TRANSLUCENT = register("transclucent", new TBSuperpower(
            AbilityBuilder.of("translucent", TBAbilityTypes.TRANSLUCENT)
                    .additionalData(a -> new IHasOverlay(a, (b) -> b.uOffset(16)))
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.TOGGLE), "enabling"),
            AbilityBuilder.of("damage_resistance", AbilityType.DAMAGE_RESISTANCE).additionalData((a) -> a.dataManager.set(DamageResistanceAbility.AMPLIFIER, 4F)),
            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE.value()).amount(1.0D).operation(AttributeModifier.Operation.ADD_VALUE)),
            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH.value()).amount(5.0D).operation(AttributeModifier.Operation.ADD_VALUE)),
            AttributeModifierAbility.of("armor", b -> b.attribute(Attributes.ARMOR.value()).amount(5.0D).operation(AttributeModifier.Operation.ADD_VALUE)),
            AttributeModifierAbility.of("fall_resistance", b -> b.attribute(ModRegistries.FALL_RESISTANCE.value()).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADD_VALUE))
    ));

//    public static final Superpower STARLIGHT = register("starlight", new TBSuperpower(
//            AbilityBuilder.of("lasers", TBAbilityTypes.GLOW_EYES)
//                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.HELD), "enabling")
//                    .change(TBCommonUtil.COLOR, Color.YELLOW),
//            AbilityBuilder.of("energy_charging", TBAbilityTypes.ENERGY_CHARGING)
//                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.HELD), "enabling"),
//            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(1.0D).operation(AttributeModifier.Operation.ADD_VALUE)),
//            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(5.0D).operation(AttributeModifier.Operation.ADD_VALUE)),
//            AttributeModifierAbility.of("jump_boost", b -> b.attribute(ModRegistries.JUMP_BOOST).amount(1.0D).operation(AttributeModifier.Operation.ADD_VALUE)),
//            AttributeModifierAbility.of("fall_resistance", b -> b.attribute(ModRegistries.FALL_RESISTANCE).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADD_VALUE))
//    ));
//
//    public static final Superpower TEST = register("test", new TBSuperpower(
//            AbilityBuilder.of("speed", TBAbilityTypes.SPEED).change(TBCommonUtil.COLOR, Color.RED)
//                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.TOGGLE), "enabling"),
//            AbilityBuilder.of("lasers", TBAbilityTypes.HEAT_VISION).change(TBCommonUtil.COLOR, Color.BLUE)
//                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.SECOND).action(KeyCondition.Action.HELD), "enabling"),
//
//            AbilityBuilder.of("bruh", TBAbilityTypes.BRUH)
//                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.THIRD)
//                            .action(KeyCondition.Action.HELD), "enabling")
//    ));

    public static <T extends TBSuperpower> T register(String id, T item) {
        return Registry.register(Superpower.REGISTRY, TheBoys.id(id), item);
    }

    public static void init() {

    }
}
