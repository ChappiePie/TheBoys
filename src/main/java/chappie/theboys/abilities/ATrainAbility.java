package chappie.theboys.abilities;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;
import xyz.heroesunited.heroesunited.common.objects.HUAttributes;

import java.awt.*;
import java.util.UUID;

public class ATrainAbility extends SpeedAbility {

    private final UUID ATTRIBUTE_UUID = UUID.fromString("931d9761-4e36-46a6-ab4f-7a2e38d80d3b");

    public void onActivated(PlayerEntity player) {
        super.onActivated(player);
        AbilityHelper.setAttribute(player, "a-train", Attributes.MAX_HEALTH, ATTRIBUTE_UUID, 5D, AttributeModifier.Operation.ADDITION);
        AbilityHelper.setAttribute(player, "a-train", Attributes.ATTACK_DAMAGE, ATTRIBUTE_UUID, 1.0D, AttributeModifier.Operation.ADDITION);
        AbilityHelper.setAttribute(player, "a-train", HUAttributes.FALL_RESISTANCE, ATTRIBUTE_UUID, -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public void onDeactivated(PlayerEntity player) {
        super.onDeactivated(player);
        AbilityHelper.setAttribute(player, "a-train", Attributes.MAX_HEALTH, ATTRIBUTE_UUID, 0D, AttributeModifier.Operation.ADDITION);
        AbilityHelper.setAttribute(player, "a-train", Attributes.ATTACK_DAMAGE, ATTRIBUTE_UUID, 0D, AttributeModifier.Operation.ADDITION);
        AbilityHelper.setAttribute(player, "a-train", HUAttributes.FALL_RESISTANCE, ATTRIBUTE_UUID, 0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
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
