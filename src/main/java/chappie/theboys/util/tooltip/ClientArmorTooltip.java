package chappie.theboys.util.tooltip;

import chappie.theboys.TheBoys;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ClientArmorTooltip implements ClientTooltipComponent {
    private static final ResourceLocation SLOT_SPRITE = TheBoys.id("textures/gui/armor_slot.png");
    private final ItemStack itemStack;

    public ClientArmorTooltip(ArmorTooltip tooltip) {
        this.itemStack = tooltip.itemStack();
    }

    @Override
    public int getHeight(Font font) {
        return this.gridSizeY() * 18;
    }

    @Override
    public int getWidth(Font pFont) {
        return this.gridSizeX() * 20 + pFont.width(this.itemStack.getHoverName().getString()) + 2;
    }

    @Override
    public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics guiGraphics) {
        ClientTooltipComponent.super.renderImage(font, x, y, width, height, guiGraphics);
        int i = this.gridSizeX();
        int j = this.gridSizeY();
        int k = 0;

        for (int l = 0; l < j; ++l) {
            for (int i1 = 0; i1 < i; ++i1) {
                int j1 = x + i1 * 18 + 1;
                int k1 = y + l * 20 + 1;
                this.renderSlot(j1, k1, k++, font, guiGraphics);
            }
        }
    }

    private void renderSlot(int pX, int pY, int pItemIndex, Font pFont, GuiGraphics guiGraphics) {
        guiGraphics.renderItem(this.itemStack, pX + 1, pY + 1, pItemIndex);
        guiGraphics.blitSprite(RenderType::guiTextured, SLOT_SPRITE, pX, pY, 0, 18, 20);
        guiGraphics.drawString(pFont, this.itemStack.getHoverName().getString(), pX + this.gridSizeX() * 20 + 3, pY + 5, -1, true);
        guiGraphics.renderItemDecorations(pFont, this.itemStack, pX + 1, pY + 1);
    }

    private int gridSizeX() {
        return (int) Math.ceil(Math.sqrt(1));
    }

    private int gridSizeY() {
        return (int) Math.ceil(1 / (double) this.gridSizeX());
    }
}