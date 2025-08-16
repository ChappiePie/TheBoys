package chappie.theboys.mixin.client;

import chappie.theboys.client.ClientEvents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    // TODO test too
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"), method = "renderLevel(Lnet/minecraft/client/DeltaTracker;)V", cancellable = true)
    private void renderLevel(DeltaTracker deltaTracker, CallbackInfo ci) {
        PoseStack poseStack = new PoseStack();
        ClientEvents.setupRoll(deltaTracker.getGameTimeDeltaTicks(), poseStack);
    }
}