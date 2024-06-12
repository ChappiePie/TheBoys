package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.theboys.client.gui.EyeOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

public class BruhAbility extends Ability {
    public BruhAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        if (enabled && entity.level.isClientSide) {
            Minecraft.getInstance().setScreen(new EyeOptionsScreen(null));
        }
    }
}
