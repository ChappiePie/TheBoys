package chappie.theboys.mixin.client;

import chappie.theboys.util.TranslucentBlocksUtil;
import chappie.theboys.util.interfaces.IIndigoAlphaContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractRenderContext;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@SuppressWarnings("UnstableApiUsage")
@Pseudo
@Mixin(AbstractRenderContext.class)
public abstract class BlockRendererMixin {

    @ModifyArg(
            method = "bufferQuad(Lnet/fabricmc/fabric/impl/client/indigo/renderer/mesh/MutableQuadViewImpl;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;addVertex(FFFIFFIIFFF)V"
            ),
            index = 3
    )
    private int theBoys$fixAlpha(int color) {
        float alpha = TranslucentBlocksUtil.NO_ALPHA;
        if ((Object) this instanceof IIndigoAlphaContext ctx) {
            alpha = ctx.theBoys$getIndigoAlpha();
        }
        if (alpha == TranslucentBlocksUtil.NO_ALPHA) {
            return color;
        }
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        return (color & 0x00FFFFFF) | (a << 24);
    }
}
