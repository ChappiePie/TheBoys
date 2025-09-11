package chappie.theboys.mixin.client;

import chappie.theboys.util.TranslucentBlocksUtil;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {

    @Unique
    private final TranslucentBlocksUtil translucentBlocksUtil = new TranslucentBlocksUtil();

    @Shadow protected abstract BufferBuilder getOrBeginLayer(Map<RenderType, BufferBuilder> bufferLayers, SectionBufferBuilderPack sectionBufferBuilderPack, RenderType renderType);

    @WrapWithCondition(method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;)V"))
    public boolean compile(BlockRenderDispatcher instance, BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource random, @Local Map<RenderType, BufferBuilder> map, @Local(argsOnly = true) SectionBufferBuilderPack sectionBufferBuilderPack) {
        return this.translucentBlocksUtil.blockCompile(instance, state, pos, level, poseStack, this.getOrBeginLayer(map, sectionBufferBuilderPack, RenderType.translucent()));
    }
}
