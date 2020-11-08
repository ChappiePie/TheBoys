package chappie.theboys.abilities;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;
import xyz.heroesunited.heroesunited.util.HUAttributes;

import java.awt.*;
import java.util.UUID;

public class ATrainAbility extends SpeedAbility {

    private final UUID ATTRIBUTE_UUID = UUID.fromString("931d9761-4e36-46a6-ab4f-7a2e38d80d3b");

    public void onActivated(PlayerEntity player) {
        super.onActivated(player);
        AbilityHelper.addAttribute(player, Attributes.MAX_HEALTH, 5D, AttributeModifier.Operation.ADDITION, ATTRIBUTE_UUID);
        AbilityHelper.addAttribute(player, Attributes.ATTACK_DAMAGE, 1.0D, AttributeModifier.Operation.ADDITION, ATTRIBUTE_UUID);
        AbilityHelper.addAttribute(player, HUAttributes.FALL_RESISTANCE, -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL, ATTRIBUTE_UUID);
    }

    public void onDeactivated(PlayerEntity player) {
        super.onDeactivated(player);
        AbilityHelper.removeAttribute(player, HUAttributes.FALL_RESISTANCE, ATTRIBUTE_UUID);
        AbilityHelper.removeAttribute(player, Attributes.MAX_HEALTH, ATTRIBUTE_UUID);
        AbilityHelper.removeAttribute(player, Attributes.ATTACK_DAMAGE, ATTRIBUTE_UUID);
    }

    @Override
    public int getLifeTimeForTrail() {
        return 10;
    }

    @Override
    public Color getTrailColor() {
        return new Color(24, 76, 153);
    }

    @Override
    public int getMaxSpeedLevel(PlayerEntity player) {
        return 40;
    }
}
