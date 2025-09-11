package chappie.theboys.mixin;

import chappie.theboys.util.interfaces.IWithAlpha;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockPos.class)
public class BlockPosMixin implements IWithAlpha {

    @Unique
    private float alpha = -1;


    @Override
    public void theBoys$setAlpha(float alpha) {
        this.alpha = alpha;
    }

    @Override
    public float theBoys$getAlpha() {
        return this.alpha;
    }
}
