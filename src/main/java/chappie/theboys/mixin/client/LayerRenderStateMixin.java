package chappie.theboys.mixin.client;

import chappie.theboys.util.interfaces.IItemRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public class LayerRenderStateMixin implements IItemRenderState {

    @Unique
    private ItemStackRenderState itemStackRenderState;

    @Override
    public ItemStackRenderState theBoys$getRenderState() {
        if (this.itemStackRenderState == null) {
            this.itemStackRenderState = new ItemStackRenderState();
        }
        return this.itemStackRenderState;
    }

    @Inject(method = "clear", at = @At("TAIL"))
    private void clearStack(CallbackInfo ci) {
        if (this.itemStackRenderState != null) {
            this.itemStackRenderState.clear();
        }
    }

    @Inject(method = "submit", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void renderStack(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay, int outlineColor, CallbackInfo ci) {
        if (this.itemStackRenderState != null) {
            this.itemStackRenderState.submit(poseStack, nodeCollector, packedLight, packedOverlay, 0);
        }
    }
}
