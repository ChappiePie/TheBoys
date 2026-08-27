package chappie.theboys.mixin.client;

import chappie.theboys.util.TranslucentBlocksUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AltModelBlockRendererImpl;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Pseudo
@Mixin(AltModelBlockRendererImpl.class)
public abstract class BlockRenderInfoMixin {

    @Shadow
    @Final
    private BlockPos.MutableBlockPos scratchPos;
    @Shadow
    private BlockPos pos;

    @Unique
    private boolean theBoys$renderAsTranslucent;
    @Unique
    private boolean theBoys$transformPushed;

    @Inject(method = "tesselateBlock(Lnet/fabricmc/fabric/api/client/renderer/v1/mesh/QuadEmitter;FFFLnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;J)V",
            at = @At("HEAD"))
    private void theBoys$pushAlphaTransform(QuadEmitter output, float x, float y, float z, BlockAndTintGetter level, BlockPos pos, BlockState blockState, BlockStateModel model, long seed, CallbackInfo ci) {
        float alpha = TranslucentBlocksUtil.resolveAlpha(pos);
        this.theBoys$renderAsTranslucent = alpha != TranslucentBlocksUtil.NO_ALPHA;

        if (this.theBoys$renderAsTranslucent) {
            int packedAlpha = Mth.clamp((int) (alpha * 255.0F), 0, 255);
            QuadTransform alphaTransform = quad -> {
                quad.chunkLayer(ChunkSectionLayer.TRANSLUCENT);
                for (int i = 0; i < 4; i++) {
                    int color = quad.color(i);
                    color = (color & 0x00FFFFFF) | (packedAlpha << 24);
                    quad.color(i, color);
                }
                return true;
            };
            output.pushTransform(alphaTransform);
            this.theBoys$transformPushed = true;
        } else {
            this.theBoys$transformPushed = false;
        }
    }

    @Inject(method = "tesselateBlock(Lnet/fabricmc/fabric/api/client/renderer/v1/mesh/QuadEmitter;FFFLnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;J)V",
            at = @At("RETURN"))
    private void theBoys$popAlphaTransform(QuadEmitter output, float x, float y, float z, BlockAndTintGetter level, BlockPos pos, BlockState blockState, BlockStateModel model, long seed, CallbackInfo ci) {
        if (this.theBoys$transformPushed) {
            output.popTransform();
            this.theBoys$transformPushed = false;
        }
    }

    @WrapOperation(
            method = "shouldCullFace(Lnet/minecraft/core/Direction;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"
            )
    )
    private boolean theBoys$forceFaceVisibility(BlockState state, BlockState neighborState, Direction direction, Operation<Boolean> original) {
        if (!this.theBoys$renderAsTranslucent && TranslucentBlocksUtil.canSeeThrough(this.scratchPos.setWithOffset(pos, direction))) {
            return true;
        }
        return original.call(state, neighborState, direction);
    }
}
