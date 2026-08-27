package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.theboys.TheBoys;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class WaterMiningAbility extends Ability {
    public WaterMiningAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        if (!entity.level().isClientSide()) {
            this.setAttribute(entity, this.builder.id + "_submerged_mining", Attributes.SUBMERGED_MINING_SPEED.value(), enabled ? 0.8D : 0.0D, AttributeModifier.Operation.ADD_VALUE);
        }
    }

    private void setAttribute(LivingEntity entity, String name, Attribute attribute, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute));
        Identifier location = TheBoys.id(name);

        if (instance != null) {
            var modifier = instance.getModifier(location);
            if (modifier != null && modifier.amount() != amount) {
                instance.removeModifier(location);
                modifier = null;
            }
            if (modifier == null && amount != 0.0F) {
                instance.addTransientModifier(new AttributeModifier(location, amount, operation));
            }
        }
    }
}