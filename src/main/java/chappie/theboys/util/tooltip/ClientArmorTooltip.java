package chappie.theboys.util.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

public class ClientArmorTooltip implements ClientTooltipComponent {
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
    public void renderImage(Font pFont, int pMouseX, int pMouseY, PoseStack pPoseStack, ItemRenderer pItemRenderer) {
        int i = this.gridSizeX();
        int j = this.gridSizeY();
        int k = 0;

        for(int l = 0; l < j; ++l) {
            for(int i1 = 0; i1 < i; ++i1) {
                int j1 = pMouseX + i1 * 20;
                int k1 = pMouseY + l * 20;
                this.renderSlot(j1, k1, k++, pFont, pPoseStack, pItemRenderer, 0);
            }
        }
    }

    private void renderSlot(int pX, int pY, int pItemIndex, Font pFont, PoseStack pPoseStack, ItemRenderer pItemRenderer, int pBlitOffset) {
        ItemStack itemstack = this.itemStack;
        this.blit(pPoseStack, pX, pY, pBlitOffset, false);
        pItemRenderer.renderAndDecorateItem(pPoseStack, itemstack, pX + 1, pY + 1, pItemIndex);
        pFont.drawShadow(pPoseStack, itemstack.getHoverName().getString(), pX + this.gridSizeX() * 20 + 3, pY + 5, -1);
        pItemRenderer.renderGuiItemDecorations(pPoseStack, pFont, itemstack, pX + 1, pY + 1);
    }

    private void blit(PoseStack pPoseStack, int pX, int pY, int pBlitOffset, boolean blocked) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, ClientBundleTooltip.TEXTURE_LOCATION);
        GuiComponent.blit(pPoseStack, pX, pY, pBlitOffset, 0, blocked ? 40 : 0, 18, 18, 128, 128);
    }

    private int gridSizeX() {
        return (int)Math.ceil(Math.sqrt(1));
    }

    private int gridSizeY() {
        return (int)Math.ceil(1 / (double)this.gridSizeX());
    }
}