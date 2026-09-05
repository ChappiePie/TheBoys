package chappie.theboys.util.tooltip;

import chappie.modulus.util.ClientUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public class ClientArmorTooltip implements ClientTooltipComponent {
    private final ItemStack itemStack;

    public ClientArmorTooltip(ArmorTooltip tooltip) {
        this.itemStack = tooltip.itemStack();
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public int getWidth(Font pFont) {
        return 20 + pFont.width(this.itemStack.getHoverName().getString()) + 14;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        ClientTooltipComponent.super.renderImage(font, x, y, guiGraphics);
        this.renderSlot(x + 1, y + 1, 0, font, guiGraphics);
    }

    private void renderSlot(int pX, int pY, int pItemIndex, Font pFont, GuiGraphics guiGraphics) {
        guiGraphics.renderItem(this.itemStack, pX + 1, pY + 1, pItemIndex);

        int color = ClientUtil.ARGB.color(57, 46, 86);
        int colorLight = ClientUtil.ARGB.color(37, 26, 66);
        int colorDark = ClientUtil.ARGB.color(77, 66, 106);

        int b = 2;
        guiGraphics.fill(pX + b, pY + b, pX + 18 + b, pY + 18 + b, colorLight);
        guiGraphics.fill(pX - b, pY - b, pX + 18 - b, pY + 18 - b, colorDark);
        guiGraphics.fill(pX, pY, pX + 18, pY + 18, color);

        String s = this.itemStack.getHoverName().getString();
        int x = pX + 20 + 1 + 5;
        int y = pY + b;
        int xMax = x + 4 + pFont.width(s);
        int yMax = pY + pFont.lineHeight + 8;

        guiGraphics.fill(x + b, y + b, xMax + b, yMax + b, colorLight);
        guiGraphics.fill(x - b, y - b, xMax - b, yMax - b, colorDark);
        guiGraphics.fill(x, y, xMax, yMax, color);

        guiGraphics.drawString(pFont, s, x + b, pY + 5, -1, true);
        guiGraphics.renderItemDecorations(pFont, this.itemStack, pX + 1, pY + 1);
    }
}