package chappie.theboys.common.ability.base;

import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.common.ability.base.condition.KeyCondition;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.KeyMap;
import chappie.modulus.util.ModRegistries;
import chappie.theboys.TheBoys;
import chappie.theboys.common.ability.*;
import chappie.theboys.common.ability.interfaces.IHasOverlay;
import chappie.theboys.util.TBCommonUtil;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.EntityHitResult;

import java.awt.*;

public class TBSuperpowers {
    public static final Superpower HOMELANDER = register("homelander", new TBSuperpower(
            HeatVisionAbility.of("lasers", k -> k.keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.HELD),
                            k -> k.keyType(KeyMap.KeyType.MOUSE_RIGHT).action(KeyCondition.Action.HELD))
                    .change(TBCommonUtil.COLOR, Color.RED)
                    .additionalData(a ->
                            new IHasOverlay(a, (b) -> b.uOffset(48)
                                    .backgroundColor(() -> a.conditionManager.test("eyes") ? 16711680 : -1)
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
                    .additionalData(a -> new IHasOverlay(a, (b) -> b.uOffset(16)))
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.THIRD).action(KeyCondition.Action.HELD), "enabling"),

            AbilityBuilder.of("damage_resistance", TBAbilityTypes.DAMAGE_RESISTANCE).additionalData((a) -> a.dataManager.set(DamageResistanceAbility.AMPLIFIER, 8F)),
            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(2.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(10.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("jump_boost", b -> b.attribute(ModRegistries.JUMP_BOOST).amount(1.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("fall_resistance", b -> b.attribute(ModRegistries.FALL_RESISTANCE).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADDITION)),
            DamageImmunityAbility.of("fire_immunity", "lava", "onFire", "inFire", "hotFloor"))
            .uOffset(16));

    public static final Superpower A_TRAIN = register("a_train", new TBSuperpower(
            AbilityBuilder.of("speed", TBAbilityTypes.SPEED)
                    .change(TBCommonUtil.COLOR, Color.BLUE)
                    .additionalData(a -> new IHasOverlay(a, (b) -> b.uOffset(16)))
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.TOGGLE), "enabling"),
            AbilityBuilder.of("focus", TBAbilityTypes.FOCUS_ON_GOAL)
                    .additionalData(a -> new IHasOverlay(a, (b) -> b.uOffset(16).backgroundColor(() -> {
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
            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(1.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(1.0D).operation(AttributeModifier.Operation.ADDITION))
    ).uOffset(32));

    public static final Superpower TRANSLUCENT = register("transclucent", new TBSuperpower(
            AbilityBuilder.of("translucent", TBAbilityTypes.TRANSLUCENT)
                    .additionalData(a -> new IHasOverlay(a, (b) -> b.uOffset(16)))
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.TOGGLE), "enabling"),
            AbilityBuilder.of("damage_resistance", TBAbilityTypes.DAMAGE_RESISTANCE).additionalData((a) -> a.dataManager.set(DamageResistanceAbility.AMPLIFIER, 4F)),
            AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(1.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(5.0D).operation(AttributeModifier.Operation.ADDITION)),
            AttributeModifierAbility.of("armor", b -> b.attribute(Attributes.ARMOR).amount(5.0D).operation(AttributeModifier.Operation.ADDITION)),
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
