package chappie.theboys.util.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;

public record ClientArmorTooltip(ItemStack itemStack) implements ClientTooltipComponent {

    @Override
    public int getHeight(Font font) {
        return this.gridSizeY() * 18;
    }

    @Override
    public int getWidth(Font pFont) {
        return this.gridSizeX() * 20 + pFont.width(this.itemStack.getHoverName().getString()) + 14;
    }

    @Override
    public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics guiGraphics) {
        ClientTooltipComponent.super.renderImage(font, x, y, width, height, guiGraphics);
        int i = this.gridSizeX();
        int j = this.gridSizeY();
        int k = 0;

        for (int l = 0; l < j; ++l) {
            for (int i1 = 0; i1 < i; ++i1) {
                int j1 = x + i1 * 20 + 1;
                int k1 = y + l * 20 + 1;
                this.renderSlot(j1, k1, k++, font, guiGraphics);
            }
        }
    }

    private void renderSlot(int pX, int pY, int pItemIndex, Font pFont, GuiGraphics guiGraphics) {
        guiGraphics.renderItem(this.itemStack, pX + 1, pY + 1, pItemIndex);

        int color = ARGB.color(57, 46, 86);
        int colorLight = ARGB.color(37, 26, 66);
        int colorDark = ARGB.color(77, 66, 106);

        int b = 2;
        guiGraphics.fill(pX + b, pY + b, pX + 18 + b, pY + 18 + b, colorLight);
        guiGraphics.fill(pX - b, pY - b, pX + 18 - b, pY + 18 - b, colorDark);
        guiGraphics.fill(pX, pY, pX + 18, pY + 18, color);

        String s = this.itemStack.getHoverName().getString();
        int x = pX + this.gridSizeX() * 20 + 1 + 5;
        int y = pY + b;
        int xMax = x + 4 + pFont.width(s);
        int yMax = pY + pFont.lineHeight + 8;

        guiGraphics.fill(x + b, y + b, xMax + b, yMax + b, colorLight);
        guiGraphics.fill(x - b, y - b, xMax - b, yMax - b, colorDark);
        guiGraphics.fill(x, y, xMax, yMax, color);

        guiGraphics.drawString(pFont, s, x + b, pY + 5, -1, true);
        guiGraphics.renderItemDecorations(pFont, this.itemStack, pX + 1, pY + 1);
    }

    private int gridSizeX() {
        return (int) Math.ceil(Math.sqrt(1));
    }

    private int gridSizeY() {
        return (int) Math.ceil(1 / (double) this.gridSizeX());
    }
}