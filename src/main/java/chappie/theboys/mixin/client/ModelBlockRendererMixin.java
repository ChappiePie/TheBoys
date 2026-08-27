package chappie.theboys.mixin.client;

import chappie.theboys.util.TranslucentBlocksUtil;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {

    /**
     * Force faces to render when the neighboring block is within XRay zone.
     */
    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
    private void theBoys$forceRenderFaceForXRay(
            BlockAndTintGetter level, BlockState state, Direction direction, BlockPos neighborPos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (TranslucentBlocksUtil.canSeeThrough(neighborPos)) {
            cir.setReturnValue(true);
        }
    }
}
