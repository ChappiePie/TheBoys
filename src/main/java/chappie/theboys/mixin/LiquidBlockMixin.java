package chappie.theboys.mixin;

import chappie.theboys.abilities.SpeedAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;

import static net.minecraft.tags.FluidTags.WATER;

@Mixin(LiquidBlock.class)
public abstract class LiquidBlockMixin {

    @Shadow public abstract FlowingFluid getFluid();

    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    public void getNewCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext, CallbackInfoReturnable<VoxelShape> cir) {
        if (pContext instanceof EntityCollisionContext context && context.getEntity() instanceof LivingEntity entity && this.getFluid().is(WATER)) {
            if (entity.getOnPos().equals(pPos)) {
                for (SpeedAbility ability : AbilityHelper.getListOfType(SpeedAbility.class, AbilityHelper.getAbilities(entity))) {
                    if (ability.getEnabled() && entity.getDeltaMovement().horizontalDistanceSqr() >= 1.0E-7D) {
                        cir.setReturnValue(SpeedAbility.STABLE_SHAPE);
                    }
                }
            }
        }
    }
}
