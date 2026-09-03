package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.condition.Condition;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;

public class WaterCondition extends Condition {
    public WaterCondition(Ability ability) {
        super(ability, (c) -> {
            Level level = ability.getEntity().level();
            BlockPos playerPos = ability.getEntity().blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (int y = 0; y <= 10; y++) {
                mutableBlockPos.set(playerPos.getX(), playerPos.getY() + y, playerPos.getZ());
                if (level.getFluidState(mutableBlockPos).is(FluidTags.WATER) && !level.getFluidState(mutableBlockPos.above()).is(FluidTags.WATER)) {
                    return true;
                }
            }
            return false;
        });
    }
}
