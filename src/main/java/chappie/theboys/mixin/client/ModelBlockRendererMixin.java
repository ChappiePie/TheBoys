package chappie.theboys.mixin.client;

import chappie.theboys.util.TranslucentBlocksUtil;
import chappie.theboys.util.interfaces.IWithAlpha;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {

    @ModifyConstant(method = "putQuadData", constant = @Constant(floatValue = 1.0F))
    private float theBoys$blockAlpha(float constant, @Local(argsOnly = true) BlockPos pos) {
        if (pos instanceof IWithAlpha a && a.theBoys$getAlpha() != -1) {
            return a.theBoys$getAlpha();
        }
        return constant;
    }

    @WrapOperation(method = "tesselateWithAO(Lnet/minecraft/world/level/BlockAndTintGetter;Ljava/util/List;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZI)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;shouldRenderFace(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;ZLnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean theBoys$tesselateWithAO(BlockAndTintGetter level, BlockState state, boolean checkSides, Direction face, BlockPos facePos, Operation<Boolean> original, @Local(argsOnly = true) BlockPos pos) {
        if (facePos instanceof BlockPos.MutableBlockPos mutable && TranslucentBlocksUtil.canSeeThrough(mutable) && pos instanceof IWithAlpha alpha && alpha.theBoys$getAlpha() == -1) {
            return true;
        }
        return original.call(level, state, checkSides, face, facePos);
    }

    @WrapOperation(method = "tesselateWithoutAO(Lnet/minecraft/world/level/BlockAndTintGetter;Ljava/util/List;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZI)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;shouldRenderFace(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;ZLnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean theBoys$tesselateWithoutAO(BlockAndTintGetter level, BlockState state, boolean checkSides, Direction face, BlockPos facePos, Operation<Boolean> original, @Local(argsOnly = true) BlockPos pos) {
        if (facePos instanceof BlockPos.MutableBlockPos mutable && TranslucentBlocksUtil.canSeeThrough(mutable) && pos instanceof IWithAlpha alpha && alpha.theBoys$getAlpha() == -1) {
            return true;
        }
        return original.call(level, state, checkSides, face, facePos);
    }
}
