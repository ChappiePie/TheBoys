package chappie.theboys.mixin.client.sodium;

import chappie.theboys.util.TranslucentBlocksUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache")
public class MixinBlockOcclusionCache {

    @Shadow
    @Final
    private BlockPos.MutableBlockPos cachedPositionObject;

    @Inject(at = @At("HEAD"), method = "shouldDrawSide", cancellable = true, remap = false)
    private void shouldDrawSide(BlockState selfBlockState, BlockGetter view, BlockPos selfPos, Direction facing,
                                CallbackInfoReturnable<Boolean> ci) {
        if (TranslucentBlocksUtil.canSeeThrough(this.cachedPositionObject.setWithOffset(selfPos, facing))) {
            ci.setReturnValue(true);
        }
    }
}