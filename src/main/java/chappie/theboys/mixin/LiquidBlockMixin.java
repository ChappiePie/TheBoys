package chappie.theboys.mixin;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.SpeedAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.tags.FluidTags.WATER;

@Mixin(LiquidBlock.class)
public class LiquidBlockMixin {

    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    public void getNewCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext, CallbackInfoReturnable<VoxelShape> cir) {
        if (pContext.isAbove(SpeedAbility.STABLE_SHAPE, pPos, true) && pState.getValue(LiquidBlock.LEVEL) == 0) {
            if (pContext instanceof EntityCollisionContext context && context.getEntity() instanceof LivingEntity entity) {
                if (((LiquidBlock) (Object) this).getFluidState(pState).is(WATER)) {
                    for (SpeedAbility ability : CommonUtil.listOfType(SpeedAbility.class, CommonUtil.getAbilities(entity))) {
                        if (ability.isEnabled() && entity.getDeltaMovement().horizontalDistanceSqr() >= 1.0E-7D) {
                            cir.setReturnValue(SpeedAbility.STABLE_SHAPE);
                        }
                    }
                }
            }
        }
    }
}
