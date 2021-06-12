package chappie.theboys.abilities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.LogicalSide;

public interface IAbilityTick {

    void tick(PlayerEntity player, LogicalSide side);

}
