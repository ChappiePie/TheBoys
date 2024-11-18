package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.util.data.DataAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * TODO For Starlight
 */
public class EnergyChargingAbility extends Ability {

    public static final DataAccessor<Integer> ENERGY = new DataAccessor<>("energy", DataAccessor.DataSerializer.INT);

    public EnergyChargingAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        if (enabled && entity instanceof ServerPlayer player) {
            int r = 15;
            for (BlockPos pos1 : BlockPos.betweenClosed(-r, -r, -r, r, r, r)) {
                BlockPos pos = player.getOnPos().offset(pos1);
                BlockState state = player.getCommandSenderWorld().getBlockState(pos);
                if (state.getOptionalValue(BlockStateProperties.LIT).isPresent() && entity.tickCount % 8 == 0) {
                    player.getCommandSenderWorld().setBlock(pos, state.cycle(BlockStateProperties.LIT), 2);
                }
            }
        }
    }
}
