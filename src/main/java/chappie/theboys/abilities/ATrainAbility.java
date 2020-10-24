package chappie.theboys.abilities;

import chappie.theboys.common.capability.BoysCap;
import chappie.theboys.common.capability.IBoys;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import xyz.heroesunited.generatorrex.abilities.AbilityHelper;
import xyz.heroesunited.generatorrex.util.GRAttributes;
import xyz.heroesunited.generatorrex.util.GRPlayerUtil;

import java.util.List;
import java.util.UUID;

public class ATrainAbility extends SpeedAbility {

    private final UUID ATTRIBUTE_UUID = UUID.fromString("931d9761-4e36-46a6-ab4f-7a2e38d80d3b");

    public void onActivated(PlayerEntity player) {
        super.onActivated(player);
        AbilityHelper.addAttribute(player, Attributes.MAX_HEALTH, 5D, AttributeModifier.Operation.ADDITION, ATTRIBUTE_UUID);
        AbilityHelper.addAttribute(player, Attributes.ATTACK_DAMAGE, 1.0D, AttributeModifier.Operation.ADDITION, ATTRIBUTE_UUID);
        AbilityHelper.addAttribute(player, GRAttributes.FALL_RESISTANCE.get(), -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL, ATTRIBUTE_UUID);
    }

    public void onDeactivated(PlayerEntity player) {
        super.onDeactivated(player);
        AbilityHelper.removeAttribute(player, GRAttributes.FALL_RESISTANCE.get(), ATTRIBUTE_UUID);
        AbilityHelper.removeAttribute(player, Attributes.MAX_HEALTH, ATTRIBUTE_UUID);
        AbilityHelper.removeAttribute(player, Attributes.ATTACK_DAMAGE, ATTRIBUTE_UUID);
    }

    public void onUpdate(PlayerEntity player) {
        super.onUpdate(player);
        IBoys boys = BoysCap.getCap(player);
        if (boys.isInSpeed() && player.isSprinting() && boys.getSpeedLevel() > 20) {
            List<Entity> e = player.world.getEntitiesWithinAABBExcludingEntity(player, GRPlayerUtil.getCollisionBoxWithRange(GRPlayerUtil.getPlayerPos(player), 1.0D));
            if (!e.isEmpty()) {
                for (Entity entity : e) {
                    if (entity instanceof LivingEntity)
                        entity.attackEntityFrom(DamageSource.FALL, 2.0F);
                }
            }
        }
    }

    @Override
    public Vector3d getTrailColor() {
        return new Vector3d(0.5F, 0.5F, 0.8F);
    }

    @Override
    public int getMaxSpeedLevel() {
        return 40;
    }
}
