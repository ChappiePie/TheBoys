package chappie.theboys.mixin.client;

import chappie.theboys.client.ClientEvents;
import chappie.theboys.client.gui.render.pip.GuiLaserRenderer;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Final
    @Shadow private RenderBuffers renderBuffers;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"), method = "renderLevel(Lnet/minecraft/client/DeltaTracker;)V", cancellable = true)
    private void renderLevel(DeltaTracker deltaTracker, CallbackInfo ci, @Local PoseStack poseStack) {
        ClientEvents.setupRoll(deltaTracker.getGameTimeDeltaPartialTick(false), poseStack);
    }

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;<init>(Lnet/minecraft/client/renderer/state/gui/GuiRenderState;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;Ljava/util/List;)V"
            ),
            index = 2
    )
    private List<PictureInPictureRenderer<?>> theBoys$appendLaserRenderer(List<PictureInPictureRenderer<?>> renderers) {
        List<PictureInPictureRenderer<?>> mutable = new ArrayList<>(renderers);
        mutable.add(new GuiLaserRenderer());
        return mutable;
    }
}
