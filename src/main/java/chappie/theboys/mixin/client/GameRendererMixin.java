package chappie.theboys.mixin.client;

import chappie.theboys.client.ClientEvents;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"), method = "renderLevel(Lnet/minecraft/client/DeltaTracker;)V", cancellable = true)
    private void renderLevel(DeltaTracker deltaTracker, CallbackInfo ci, @Local PoseStack poseStack) {
        ClientEvents.setupRoll(deltaTracker.getGameTimeDeltaPartialTick(false), poseStack);
    }
}