package chappie.theboys.mixin.client;

import chappie.theboys.util.interfaces.SuitOverlayHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public class LayerRenderStateMixin implements SuitOverlayHolder {

    @Unique
    private ItemStackRenderState theBoys$suitOverlay;

    @Override
    public ItemStackRenderState theBoys$getSuitOverlay() {
        if (this.theBoys$suitOverlay == null) {
            this.theBoys$suitOverlay = new ItemStackRenderState();
        }
        return this.theBoys$suitOverlay;
    }

    @Inject(method = "clear", at = @At("TAIL"))
    private void theBoys$clearSuitOverlay(CallbackInfo ci) {
        if (this.theBoys$suitOverlay != null) {
            this.theBoys$suitOverlay.clear();
        }
    }

    @Inject(method = "submit", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void theBoys$submitSuitOverlay(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay, int outlineColor, CallbackInfo ci) {
        if (this.theBoys$suitOverlay != null && !this.theBoys$suitOverlay.isEmpty()) {
            this.theBoys$suitOverlay.submit(poseStack, nodeCollector, packedLight, packedOverlay, outlineColor);
        }
    }
}
