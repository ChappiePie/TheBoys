package chappie.theboys.mixin.client;

import chappie.theboys.util.TranslucentBlocksUtil;
import chappie.theboys.util.interfaces.IIndigoAlphaContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Pseudo
@Mixin(TerrainRenderContext.class)
public abstract class TerrainRenderContextMixin implements IIndigoAlphaContext {

    @Unique
    private float theBoys$currentAlpha = TranslucentBlocksUtil.NO_ALPHA;

    @Inject(method = "bufferModel(Lnet/minecraft/client/renderer/block/model/BlockStateModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
            at = @At("HEAD"))
    private void theBoys$captureAlpha(BlockStateModel model, BlockState blockState, BlockPos blockPos, CallbackInfo ci) {
        this.theBoys$currentAlpha = TranslucentBlocksUtil.resolveAlpha(blockPos);
    }

    @Inject(method = "bufferModel(Lnet/minecraft/client/renderer/block/model/BlockStateModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
            at = @At("RETURN"))
    private void theBoys$resetAlpha(BlockStateModel model, BlockState blockState, BlockPos blockPos, CallbackInfo ci) {
        this.theBoys$currentAlpha = TranslucentBlocksUtil.NO_ALPHA;
    }

    @Override
    public float theBoys$getIndigoAlpha() {
        return this.theBoys$currentAlpha;
    }
}
