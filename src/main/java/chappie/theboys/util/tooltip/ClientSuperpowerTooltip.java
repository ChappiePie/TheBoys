package chappie.theboys.util.tooltip;

import chappie.modulus.util.ClientUtil;
import chappie.theboys.common.ability.base.TBSuperpower;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

public record ClientSuperpowerTooltip(TBSuperpower superpower) implements ClientTooltipComponent {
    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/slot");

    @Override
    public int getHeight(Font font) {
        return this.gridSizeY() * 22;
    }

    @Override
    public int getWidth(Font pFont) {
        return this.gridSizeX() * 20 + pFont.width(this.superpower.getDisplayName().getString()) + 8;
    }

    @Override
    public void renderImage(Font pFont, int pX, int pY, int width, int height, GuiGraphics pGuiGraphics) {
        int i = this.gridSizeX();
        int j = this.gridSizeY();

        for (int l = 0; l < j; ++l) {
            for (int i1 = 0; i1 < i; ++i1) {
                int j1 = pX + i1 * 18 + 1;
                int k1 = pY + l * 20 + 1;
                this.renderSlot(j1, k1, pFont, pGuiGraphics);
            }
        }
    }

    private void renderSlot(int pX, int pY, Font pFont, GuiGraphics guiGraphics) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, pX, pY, 0, 18, 20);
        int border = 1;
        guiGraphics.fill(pX + border, pY + border, pX + 18 + border, pY + 18 + border, ARGB.color(57, 57, 57));
        guiGraphics.fill(pX - border, pY - border, pX + 18 - border, pY + 18 - border, -1);
        guiGraphics.fill(pX, pY, pX + 18, pY + 18, ARGB.color(157, 157, 157));

        guiGraphics.drawString(pFont, this.superpower.getDisplayName().getString(), pX + this.gridSizeX() * 20 + 3, pY + 5, -1, true);
        this.superpower.renderIcon(pX + 1, pY + 1, 1, Minecraft.getInstance(), guiGraphics, ClientUtil.getPartialTick());
    }

    private int gridSizeX() {
        return (int) Math.ceil(Math.sqrt(1));
    }

    private int gridSizeY() {
        return (int) Math.ceil(1 / (double) this.gridSizeX());
    }
}
