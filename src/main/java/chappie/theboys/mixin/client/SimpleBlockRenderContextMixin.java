package chappie.theboys.mixin.client;

import chappie.theboys.util.TranslucentBlocksUtil;
import chappie.theboys.util.interfaces.IIndigoAlphaContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.SimpleBlockRenderContext;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Pseudo
@Mixin(SimpleBlockRenderContext.class)
public abstract class SimpleBlockRenderContextMixin implements IIndigoAlphaContext {

    @Unique
    private float theBoys$currentAlpha = TranslucentBlocksUtil.NO_ALPHA;

    @Inject(method = "bufferModel(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/fabricmc/fabric/api/renderer/v1/render/BlockVertexConsumerProvider;Lnet/minecraft/client/renderer/block/model/BlockStateModel;FFFIILnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At("HEAD"))
    private void theBoys$captureAlpha(PoseStack.Pose entry, BlockVertexConsumerProvider vertexConsumers, BlockStateModel model, float red, float green, float blue, int light, int overlay, BlockAndTintGetter blockView, BlockPos pos, BlockState state, CallbackInfo ci) {
        this.theBoys$currentAlpha = TranslucentBlocksUtil.resolveAlpha(pos);
    }

    @Inject(method = "bufferModel(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/fabricmc/fabric/api/renderer/v1/render/BlockVertexConsumerProvider;Lnet/minecraft/client/renderer/block/model/BlockStateModel;FFFIILnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At("RETURN"))
    private void theBoys$resetAlpha(PoseStack.Pose entry, BlockVertexConsumerProvider vertexConsumers, BlockStateModel model, float red, float green, float blue, int light, int overlay, BlockAndTintGetter blockView, BlockPos pos, BlockState state, CallbackInfo ci) {
        this.theBoys$currentAlpha = TranslucentBlocksUtil.NO_ALPHA;
    }

    @Override
    public float theBoys$getIndigoAlpha() {
        return this.theBoys$currentAlpha;
    }
}
