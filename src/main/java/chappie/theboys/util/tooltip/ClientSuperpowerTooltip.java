package chappie.theboys.util.tooltip;

import chappie.modulus.common.ability.base.Superpower;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public record ClientSuperpowerTooltip(Superpower superpower) implements ClientTooltipComponent {
    private static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot");

    @Override
    public int getHeight(Font font) {
        return this.gridSizeY() * 22;
    }

    @Override
    public int getWidth(Font pFont) {
        return this.gridSizeX() * 20 + pFont.width(this.superpower.getDisplayName().getString()) + 8;
    }

    @Override
    public void extractImage(Font pFont, int pX, int pY, int width, int height, GuiGraphicsExtractor pGuiGraphics) {
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

    private void renderSlot(int pX, int pY, Font pFont, GuiGraphicsExtractor GuiGraphicsExtractor) {
        GuiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, pX, pY, 0, 18, 20);
        int border = 1;
        GuiGraphicsExtractor.fill(pX + border, pY + border, pX + 18 + border, pY + 18 + border, ARGB.color(57, 57, 57));
        GuiGraphicsExtractor.fill(pX - border, pY - border, pX + 18 - border, pY + 18 - border, -1);
        GuiGraphicsExtractor.fill(pX, pY, pX + 18, pY + 18, ARGB.color(157, 157, 157));

        GuiGraphicsExtractor.text(pFont, this.superpower.getDisplayName().getString(), pX + this.gridSizeX() * 20 + 3, pY + 5, -1, true);
        if (this.superpower.iconTexture() != null) {
            GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, this.superpower.iconTexture(),
                    pX + 1, pY + 1, this.superpower.iconU(), this.superpower.iconV(), 16, 16, 256, 256);
        }
    }

    private int gridSizeX() {
        return (int) Math.ceil(Math.sqrt(1));
    }

    private int gridSizeY() {
        return (int) Math.ceil(1 / (double) this.gridSizeX());
    }
}