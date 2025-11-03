package chappie.theboys.client.gui.buttons;

import chappie.theboys.TheBoys;
import chappie.theboys.client.gui.SynthesizerScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class SynthesizerStartButton extends ImageButton {
    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(TheBoys.id("button"), TheBoys.id("button_highlighted"));
    private final SynthesizerScreen screen;

    public SynthesizerStartButton(SynthesizerScreen screen, int leftPos, int topPos) {
        super(leftPos + 139, topPos + 3, 29, 18, BUTTON_SPRITES, (p) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                if (screen.getMenu().clickMenuButton(mc.player, 0) && mc.gameMode != null) {
                    mc.gameMode.handleInventoryButtonClick(screen.getMenu().containerId, 0);
                }
            }
        });
        this.screen = screen;
    }

    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        MutableComponent text = this.screen.getMenu().data.get(0) == 0 ? Component.translatable("button.theboys.synthesizer.start") : Component.translatable("button.theboys.synthesizer.stop");
        text = text.withStyle(ChatFormatting.UNDERLINE);
        int i = this.active ? 16777215 : 10526880;

        var pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate(this.getX() + this.getWidth() / 2F, this.getY() + this.getHeight() * 0.75F);
        pose.scale(0.75F, 0.75F);
        pose.translate(0, -this.getHeight() / 2F);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, text, 0, 0, i | Mth.ceil(this.alpha * 255.0F) << 24);
        pose.popMatrix();
        //guiGraphics.drawCenteredString(font, text, this.getX() + this.getWidth() / 2, this.getY(), i | Mth.ceil(this.alpha * 255.0F) << 24);
    }
}
