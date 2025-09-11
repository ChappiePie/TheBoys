package chappie.theboys.mixin.client;

import chappie.theboys.util.TranslucentBlocksUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = BlockRenderInfo.class, remap = false)
public class BlockRenderInfoMixin {
	@Shadow public BlockPos blockPos;

	@Shadow @Final private BlockPos.MutableBlockPos searchPos;

	@WrapOperation(method = "shouldDrawSide(Lnet/minecraft/core/Direction;)Z", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/Block;shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"))
	public boolean theBoys$tesselateBlock(BlockState currentFace, BlockState neighboringFace, Direction face, Operation<Boolean> original) {
		if (TranslucentBlocksUtil.canSeeThrough(this.searchPos.setWithOffset(blockPos, face))) {
            return true;
        }
		return original.call(currentFace, neighboringFace, face);
	}
}