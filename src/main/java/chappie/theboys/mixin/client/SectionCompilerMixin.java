package chappie.theboys.mixin.client;

import chappie.theboys.util.TranslucentBlocksUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {

    @WrapOperation(
            method = "compile",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;tesselateBlock(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;J)V")
    )
    private void theBoys$wrapTesselateForXRay(
            ModelBlockRenderer renderer,
            BlockQuadOutput quadOutput,
            float x, float y, float z,
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            BlockStateModel model,
            long seed,
            Operation<Void> original
    ) {
        float alpha = TranslucentBlocksUtil.resolveAlpha(pos);
        if (alpha == TranslucentBlocksUtil.NO_ALPHA || alpha >= 1.0F) {
            // Normal rendering
            original.call(renderer, quadOutput, x, y, z, level, pos, state, model, seed);
        } else {
            // XRay: render with modified alpha into translucent layer
            int alphaInt = (int) (alpha * 255);
            int colorMask = ARGB.color(alphaInt, 255, 255, 255);
            BlockQuadOutput xrayOutput = (qx, qy, qz, quad, instance) -> {
                instance.multiplyColor(colorMask);
                // Force output to translucent layer regardless of material
                quadOutput.put(qx, qy, qz, quad, instance);
            };
            original.call(renderer, xrayOutput, x, y, z, level, pos, state, model, seed);
        }
    }
}
