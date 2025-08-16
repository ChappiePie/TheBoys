package chappie.theboys.mixin.client;

import chappie.theboys.client.TBOverlays;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "renderHotbarAndDecorations", at = @At(value = "HEAD"))
    public void mixin$renderHotbarAndDecorations(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        TBOverlays.render(Minecraft.getInstance(), deltaTracker.getGameTimeDeltaTicks(), guiGraphics);
    }
}
