package chappie.theboys.mixin.client;

import chappie.theboys.client.ClientEvents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getXRot()F"), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V", cancellable = true)
    private void renderLevel(float partialTicks, long finishTimeNano, PoseStack poseStack, CallbackInfo ci) {
        ClientEvents.setupRoll(partialTicks, poseStack);
    }
}