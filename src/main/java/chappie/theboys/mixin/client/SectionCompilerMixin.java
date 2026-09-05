package chappie.theboys.mixin.client;

import chappie.theboys.client.renderer.AlphaVertexConsumer;
import chappie.theboys.util.TranslucentBlocksUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {
    @Shadow
    protected abstract BufferBuilder getOrBeginLayer(Map<RenderType, BufferBuilder> bufferLayers, SectionBufferBuilderPack sectionBufferBuilderPack, RenderType renderType);

    @WrapOperation(
            method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;Lnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V")
    )
    private void theBoys$xrayTranslucent(BlockRenderDispatcher dispatcher,
                                         BlockState state,
                                         BlockPos pos,
                                         BlockAndTintGetter level,
                                         PoseStack poseStack,
                                         VertexConsumer consumer,
                                         boolean checkSides,
                                         RandomSource random,
                                         ModelData modelData,
                                         RenderType renderType,
                                         Operation<Void> original,
                                         @Local(argsOnly = true) SectionBufferBuilderPack bufferPack,
                                         @Local Map<RenderType, BufferBuilder> layers) {
        float alpha = TranslucentBlocksUtil.resolveAlpha(pos);
        if (alpha == TranslucentBlocksUtil.NO_ALPHA) {
            original.call(dispatcher, state, pos, level, poseStack, consumer, checkSides, random, modelData, renderType);
            return;
        }

        RenderType translucent = RenderType.translucent();
        BufferBuilder translucentBuffer = getOrBeginLayer(layers, bufferPack, translucent);
        VertexConsumer alphaConsumer = new AlphaVertexConsumer(translucentBuffer, alpha);
        original.call(dispatcher, state, pos, level, poseStack, alphaConsumer, checkSides, random, modelData, translucent);
    }
}
