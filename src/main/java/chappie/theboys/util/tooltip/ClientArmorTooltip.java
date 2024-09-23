package chappie.theboys.util.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ClientArmorTooltip implements ClientTooltipComponent {
    private static final ResourceLocation BACKGROUND_SPRITE = new ResourceLocation("container/bundle/background");
    private static final ResourceLocation SLOT_SPRITE = new ResourceLocation("container/bundle/slot");
    private final ItemStack itemStack;

    public ClientArmorTooltip(ArmorTooltip tooltip) {
        this.itemStack = tooltip.itemStack();
    }

    @Override
    public int getHeight() {
        return this.gridSizeY() * 18;
    }

    @Override
    public int getWidth(Font pFont) {
        return this.gridSizeX() * 20 + pFont.width(this.itemStack.getHoverName().getString()) + 2;
    }

    @Override
    public void renderImage(Font pFont, int pX, int pY, GuiGraphics pGuiGraphics) {
        int i = this.gridSizeX();
        int j = this.gridSizeY();
        int k = 0;

        for (int l = 0; l < j; ++l) {
            for (int i1 = 0; i1 < i; ++i1) {
                int j1 = pX + i1 * 18 + 1;
                int k1 = pY + l * 20 + 1;
                this.renderSlot(j1, k1, k++, pFont, pGuiGraphics, 0);
            }
        }
    }

    private void renderSlot(int pX, int pY, int pItemIndex, Font pFont, GuiGraphics guiGraphics, int pBlitOffset) {
        guiGraphics.renderItem(this.itemStack, pX + 1, pY + 1, pItemIndex);
        this.blit(guiGraphics, pX, pY, pBlitOffset, false);
        guiGraphics.drawString(pFont, this.itemStack.getHoverName().getString(), pX + this.gridSizeX() * 20 + 3, pY + 5, -1, true);
        guiGraphics.renderItemDecorations(pFont, this.itemStack, pX + 1, pY + 1);
    }

    private void blit(GuiGraphics guiGraphics, int pX, int pY, int pBlitOffset, boolean blocked) {
        guiGraphics.blitSprite(SLOT_SPRITE, pX, pY, 0, 18, 20);
    }

    private int gridSizeX() {
        return (int) Math.ceil(Math.sqrt(1));
    }

    private int gridSizeY() {
        return (int) Math.ceil(1 / (double) this.gridSizeX());
    }
}