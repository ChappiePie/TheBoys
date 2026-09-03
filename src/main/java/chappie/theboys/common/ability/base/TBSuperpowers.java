package chappie.theboys.common.ability.base;

import chappie.modulus.client.hud.AbilityHudProperties;
import chappie.modulus.common.ability.AttributeModifierAbility;
import chappie.modulus.common.ability.DamageImmunityAbility;
import chappie.modulus.common.ability.DamageResistanceAbility;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.AbilityType;
import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.common.ability.base.condition.DoubleKeyCondition;
import chappie.modulus.common.ability.base.condition.KeyCondition;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.KeyMap;
import chappie.modulus.util.ModRegistries;
import chappie.modulus.util.data.CommonAccessors;
import chappie.theboys.TheBoys;
import chappie.theboys.client.TBOverlays;
import chappie.theboys.common.ability.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.EntityHitResult;

import java.awt.*;
import java.util.List;

public class TBSuperpowers {

    public static final Superpower HOMELANDER = Superpower.builder()
            .icon(TBOverlays.TEXTURE, 16, 128)
            .add(HeatVisionAbility.of("lasers",
                            k -> k.keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.HELD),
                            k -> k.keyType(KeyMap.KeyType.MOUSE_RIGHT).action(KeyCondition.Action.HELD))
                    .change(CommonAccessors.COLOR, Color.RED)
                    .hud(a -> new AbilityHudProperties()
                            .texture(TBOverlays.TEXTURE).uv(48, 0)
                            .backgroundColor(() -> a.conditionManager.test("eyes") ? 0xFF637F : -1)
                            .keyType(() -> !a.conditionManager.test("eyes") ?
                                    KeyMap.KeyType.FIRST : KeyMap.KeyType.MOUSE_RIGHT)))
            .add(AbilityBuilder.of("flight", TBAbilityTypes.FLIGHT)
                    .condition(a -> new DoubleKeyCondition(a).shouldStop(() -> a.getEntity().onGround() || a.getEntity().isShiftKeyDown()), "enabling")
                    .condition(a -> new DoubleKeyCondition(a).keyType(KeyMap.KeyType.SPRINT).shouldStop(() -> !(a instanceof FlightAbility f && f.cooldown.end() && a.isEnabled() && a.dataManager.get(chappie.modulus.common.ability.FlightAbility.SPRINTING))), "boost")
                    .hud(a -> new AbilityHudProperties()
                            .texture(TBOverlays.TEXTURE).uv(32, 0)
                            .keyType(() -> a.conditionManager.test("enabling") ? KeyMap.KeyType.SPRINT : KeyMap.KeyType.JUMP)))
            .add(AbilityBuilder.of("super_hearing", TBAbilityTypes.SUPER_HEARING)
                    .hud(a -> new AbilityHudProperties().texture(TBOverlays.TEXTURE).uv(64, 0).autoKey())
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.SECOND).action(KeyCondition.Action.HELD), "enabling"))
            .add(AbilityBuilder.of("xray", TBAbilityTypes.XRAY)
                    .hud(a -> new AbilityHudProperties().texture(TBOverlays.TEXTURE).uv(112, 0).autoKey())
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FOURTH).action(KeyCondition.Action.HELD), "enabling"))
            .passive("damage_resistance", AbilityType.DAMAGE_RESISTANCE, a -> a.dataManager.set(DamageResistanceAbility.AMPLIFIER, 8F))
            .add(AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(2.0D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(10.0D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(AttributeModifierAbility.of("jump_boost", b -> b.attribute(ModRegistries.JUMP_BOOST).amount(1.0D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(AttributeModifierAbility.of("fall_resistance", b -> b.attribute(ModRegistries.FALL_RESISTANCE).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(DamageImmunityAbility.of("fire_immunity", List.of(DamageTypeTags.IS_FIRE)))
            .register(TheBoys.id("homelander"));

    public static final Superpower A_TRAIN = Superpower.builder()
            .icon(TBOverlays.TEXTURE, 32, 128)
            .add(AbilityBuilder.of("speed", TBAbilityTypes.SPEED)
                    .change(CommonAccessors.COLOR, Color.BLUE)
                    .hud(a -> new AbilityHudProperties().texture(TBOverlays.TEXTURE).uv(80, 0).autoKey())
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.TOGGLE), "enabling")
                    .condition(a -> new DoubleKeyCondition(a).keyType(KeyMap.KeyType.CROUCH)
                            .shouldStop(() -> !(a instanceof SpeedAbility a1 && a1.crouchCooldown.end() && a.isEnabled())), "double_crouch"))
            .add(AbilityBuilder.of("focus", TBAbilityTypes.FOCUS_ON_GOAL)
                    .hud(a -> new AbilityHudProperties().texture(TBOverlays.TEXTURE).uv(96, 0).autoKey()
                            .backgroundColor(() -> {
                                if (a instanceof FocusOnGoalAbility a1 && !a1.hasSpeedAbility()) {
                                    if (CommonUtil.pick(a.getEntity(), 40) instanceof EntityHitResult hr && hr.getEntity() instanceof LivingEntity target) {
                                        if (target.getEyePosition().distanceTo(a.getEntity().getEyePosition()) < 2) {
                                            return 8553091;
                                        }
                                    }
                                }
                                return -1;
                            }))
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
                    }.keyType(KeyMap.KeyType.SECOND).action(KeyCondition.Action.TOGGLE), "enabling"))
            .add(AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(1.0D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(1.0D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .register(TheBoys.id("a_train"));

    public static final Superpower TRANSLUCENT = Superpower.builder()
            .add(AbilityBuilder.of("translucent", TBAbilityTypes.TRANSLUCENT)
                    .hud(a -> new AbilityHudProperties().texture(TBOverlays.TEXTURE).uv(16, 0).autoKey())
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.TOGGLE), "enabling"))
            .passive("damage_resistance", AbilityType.DAMAGE_RESISTANCE, a -> a.dataManager.set(DamageResistanceAbility.AMPLIFIER, 4F))
            .add(AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(1.0D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(2.0D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(AttributeModifierAbility.of("armor", b -> b.attribute(Attributes.ARMOR).amount(5.0D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(AttributeModifierAbility.of("fall_resistance", b -> b.attribute(ModRegistries.FALL_RESISTANCE).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADD_VALUE)))
            .register(TheBoys.id("translucent"));

    public static final Superpower THE_DEEP = Superpower.builder()
            .icon(TBOverlays.TEXTURE, 48, 128)
            .add(AbilityBuilder.of("dolphin_companion", TBAbilityTypes.DOLPHIN_COMPANION)
                    .hud(a -> new AbilityHudProperties().texture(TBOverlays.TEXTURE).uv(128, 0).autoKey())
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.HELD), "enabling"))
            .add(AbilityBuilder.of("fish_swarm", TBAbilityTypes.FISH_SWARM)
                    .hud(a -> new AbilityHudProperties().texture(TBOverlays.TEXTURE).uv(144, 0).autoKey())
                    .condition(WaterCondition::new, "enabling")
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.SECOND).action(KeyCondition.Action.HELD), "enabling"))
            .passive("water_breathing", TBAbilityTypes.WATER_BREATHING)
            .passive("water_mining", TBAbilityTypes.WATER_MINING)
            .add(AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(1.0D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(1.0D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .register(TheBoys.id("the_deep"));

    public static final Superpower BLACK_NOIR = Superpower.builder()
            .icon(TBOverlays.TEXTURE, 64, 128)
            .add(AbilityBuilder.of("nod_head", TBAbilityTypes.NOD_HEAD)
                    .hud(a -> new AbilityHudProperties().texture(TBOverlays.TEXTURE).uv(160, 0).autoKey())
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.MOUSE_LEFT).action(KeyCondition.Action.ACTION), "yes")
                    .condition(a -> new Condition(a, p -> a.isEnabled()), "yes")
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.MOUSE_RIGHT).action(KeyCondition.Action.ACTION), "no")
                    .condition(a -> new Condition(a, p -> a.isEnabled()), "no")
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.HELD), "enabling"))
            .add(AbilityBuilder.of("parkour", TBAbilityTypes.PARKOUR)
                    .hud(a -> new AbilityHudProperties().texture(TBOverlays.TEXTURE).uv(176, 0).autoKey())
                    .condition(a -> new DoubleKeyCondition(a).keyType(KeyMap.KeyType.JUMP)
                            .shouldStop(() -> !(a instanceof ParkourAbility p && p.dodgeRollHandler.canTrigger())), "roll")
                    .condition(a -> new DoubleKeyCondition(a).keyType(KeyMap.KeyType.CROUCH)
                            .shouldStop(() -> !(a instanceof ParkourAbility p && p.isEnabled())), "slide"))
            .passive("damage_resistance", AbilityType.DAMAGE_RESISTANCE, a -> a.dataManager.set(DamageResistanceAbility.AMPLIFIER, 4F))
            .add(AttributeModifierAbility.of("attack_damage", b -> b.attribute(Attributes.ATTACK_DAMAGE).amount(3.0D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(AttributeModifierAbility.of("max_health", b -> b.attribute(Attributes.MAX_HEALTH).amount(4.0D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(AttributeModifierAbility.of("jump_boost", b -> b.attribute(ModRegistries.JUMP_BOOST).amount(0.5D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(AttributeModifierAbility.of("fall_resistance", b -> b.attribute(ModRegistries.FALL_RESISTANCE).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADD_VALUE)))
            .register(TheBoys.id("black_noir"));

    public static void init() {
    }
}