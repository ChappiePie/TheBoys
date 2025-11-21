package chappie.theboys.mixin.client;

import chappie.theboys.util.TranslucentBlocksUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Pseudo
@Mixin(BlockRenderInfo.class)
public abstract class BlockRenderInfoMixin {

    @Shadow
    public BlockPos blockPos;

    @Shadow
    @Final
    private BlockPos.MutableBlockPos searchPos;

    @Shadow
    private ChunkSectionLayer defaultLayer;

    @Unique
    private boolean theBoys$renderAsTranslucent;

    @WrapOperation(method = "shouldDrawSide(Lnet/minecraft/core/Direction;)Z", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"))
    private boolean theBoys$forceFaceVisibility(BlockState currentFace, BlockState neighboringFace, Direction face, Operation<Boolean> original) {
        if (!this.theBoys$renderAsTranslucent && TranslucentBlocksUtil.canSeeThrough(this.searchPos.setWithOffset(blockPos, face))) {
            return true;
        }
        return original.call(currentFace, neighboringFace, face);
    }

    @Inject(method = "prepareForBlock", at = @At("TAIL"))
    private void theBoys$forceTranslucent(BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        this.theBoys$renderAsTranslucent = TranslucentBlocksUtil.resolveAlpha(blockPos) != TranslucentBlocksUtil.NO_ALPHA;
        if (this.theBoys$renderAsTranslucent) {
            this.defaultLayer = ChunkSectionLayer.TRANSLUCENT;
        }
    }
}
