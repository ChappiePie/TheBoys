package chappie.theboys.mixin.client;

import chappie.theboys.util.TranslucentBlocksUtil;
import chappie.theboys.util.interfaces.IWithAlpha;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {

    @ModifyConstant(method = "putQuadData(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;FFFFIIIII)V", constant = @Constant(floatValue = 1.0F))
    private float theBoys$blockAlpha(float constant, @Local(argsOnly = true) BlockPos pos) {
        if (pos instanceof IWithAlpha a && a.theBoys$getAlpha() != -1) {
            return a.theBoys$getAlpha();
        }
        return constant;
    }

    @WrapOperation(method = "tesselateWithAO(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JI)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"))
    public boolean theBoys$tesselateWithAO(BlockState currentFace, BlockState neighboringFace, Direction face, Operation<Boolean> original, @Local BlockPos.MutableBlockPos mutableBlockPos, @Local(argsOnly = true) BlockPos pos) {
        if (TranslucentBlocksUtil.canSeeThrough(mutableBlockPos) && pos instanceof IWithAlpha a && a.theBoys$getAlpha() == -1) {
            return true;
        }
        return original.call(currentFace, neighboringFace, face);
    }

    @WrapOperation(method = "tesselateWithoutAO(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JI)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"))
    public boolean theBoys$tesselateWithoutAO(BlockState currentFace, BlockState neighboringFace, Direction face, Operation<Boolean> original, @Local BlockPos.MutableBlockPos mutableBlockPos, @Local(argsOnly = true) BlockPos pos) {
        if (TranslucentBlocksUtil.canSeeThrough(mutableBlockPos) && pos instanceof IWithAlpha a && a.theBoys$getAlpha() == -1) {
            return true;
        }
        return original.call(currentFace, neighboringFace, face);
    }
}
