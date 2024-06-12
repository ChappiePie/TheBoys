package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.theboys.common.ability.base.TBAbilityTypes;
import chappie.theboys.util.TBCommonUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;
import java.util.function.Consumer;

public class AttributeModifierAbility extends Ability {
    public final AttributeBuilder attributeBuilder = new AttributeBuilder();

    public AttributeModifierAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        TBCommonUtil.setAttribute(entity, this.builder.id, this.attributeBuilder.attribute,
                this.attributeBuilder.uuid, enabled ? this.attributeBuilder.amount : 0, this.attributeBuilder.operation);
        entity.setHealth(entity.getHealth());
    }

    public static AbilityBuilder of(String id, Consumer<AttributeBuilder> consumer) {
        return AbilityBuilder.of(id, TBAbilityTypes.ATTRIBUTE_MODIFIER.get()).additionalData(a -> {
            if (a instanceof AttributeModifierAbility ability) {
                consumer.accept(ability.attributeBuilder);
            }
        });
    }

    public static class AttributeBuilder {
        protected Attribute attribute;
        protected final UUID uuid = UUID.randomUUID();
        protected double amount;
        protected AttributeModifier.Operation operation;

        public AttributeBuilder attribute(Attribute attribute) {
            this.attribute = attribute;
            return this;
        }

        public AttributeBuilder amount(double amount) {
            this.amount = amount;
            return this;
        }

        public AttributeBuilder operation(AttributeModifier.Operation operation) {
            this.operation = operation;
            return this;
        }
    }
}