package chappie.theboys.client.gui;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.IHasTimer;
import chappie.theboys.TheBoys;
import chappie.theboys.client.gui.buttons.SynthesizerStartButton;
import chappie.theboys.common.block.entity.SynthesizerBlockEntity;
import chappie.theboys.common.block.menu.SynthesizerMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SynthesizerScreen extends AbstractContainerScreen<SynthesizerMenu> {
    private static final ResourceLocation SYNTHESIZER_LOCATION = TheBoys.id("textures/gui/synthesizer.png");
    public static IHasTimer.Timer timer = new IHasTimer.Timer(() -> 10, () -> false);
    public static IHasTimer.Timer rollTimer = new SynthesizerBlockEntity.RollTimer(() -> false);

    private SynthesizerStartButton startButton;

    public SynthesizerScreen(SynthesizerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    public static void renderTooltip(GuiGraphics guiGraphics, Component toolTip, int pMouseX, int pMouseY, int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = guiGraphics.pose();
        boolean isMouseOverObj = pMouseX >= x && pMouseY >= y && pMouseX <= x + width && pMouseY <= y + height;
        if (isMouseOverObj) {
            poseStack.pushPose();
            poseStack.translate(0, 0, 50);
            int i = pMouseX + 2;
            int j = pMouseY - 10;
            int k = mc.font.width(toolTip);
            guiGraphics.fillGradient(i - 3, j - 3, i + k + 3, j + 8 + 3, -1073741824, -1073741824);
            guiGraphics.drawString(mc.font, toolTip, i, j, 16777215, true);
            poseStack.popPose();
        }
    }

    @Override
    protected void init() {
        super.init();
        this.startButton = this.addRenderableWidget(new SynthesizerStartButton(this, this.leftPos, this.topPos));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        rollTimer.predicate = () -> this.getMenu().getBurnProgress() != 0;
        timer.predicate = () -> this.getMenu().getBurnProgress() != 0;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderType::guiTextured, SYNTHESIZER_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        float f = this.getMenu().data.get(1) / 500F;

        int progress = (int) (37 * f);
        guiGraphics.blit(RenderType::guiTextured, SYNTHESIZER_LOCATION, i + 16, j + 10 + 37 - progress, 190, 37 - progress, 12, progress, 256, 256);

        int progress1 = Mth.ceil(this.menu.getBurnProgress() * 16.0F);
        guiGraphics.blit(RenderType::guiTextured, SYNTHESIZER_LOCATION, i + 145, j + 28, 202, 0, progress1, 5, 256, 256);


        int progress2 = Mth.ceil(this.menu.getLitProgress() * 14.0F);
        guiGraphics.blit(RenderType::guiTextured, SYNTHESIZER_LOCATION, i + 146, j + 37 + 14 - progress2, 176, 14 - progress2, 14, progress2, 256, 256);


        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(i, j, 0);
        guiGraphics.pose().pushPose();
        guiGraphics.enableScissor(this.leftPos + 36, this.topPos, this.leftPos + 36 + 103, this.topPos + 81);

        guiGraphics.pose().translate(87.5F, 40.5F, 0);
        guiGraphics.pose().mulPose(Axis.ZN.rotationDegrees(360F * rollTimer.value(ClientUtil.getPartialTick())));
        guiGraphics.pose().translate(-87.5F, -40.5F, 0);

        for (Slot slot : this.menu.slots) {
            if (slot instanceof SynthesizerMenu.CentrifugeSlot) {
                guiGraphics.blit(RenderType::guiTextured, SYNTHESIZER_LOCATION, slot.x - 4, slot.y - 4, 218, 54, 24, 24, 256, 256);
                super.renderSlot(guiGraphics, slot);
            }
        }
        guiGraphics.disableScissor();
        guiGraphics.pose().popPose();

        Slot mainSlot = this.menu.slots.get(2);
        if (!mainSlot.isActive()) {
            this.renderSlot(guiGraphics, mainSlot);
        }
        guiGraphics.pose().popPose();
    }

    @Override
    protected void renderSlot(@NotNull GuiGraphics guiGraphics, Slot slot) {
        float f = Math.max(0.4F, 1.0F - timer.value(ClientUtil.getPartialTick()));
        if (slot.index == 2) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(slot.x - 4, slot.y - 4, 0);
            guiGraphics.pose().translate(12.5F, 12.5F, 0);
            guiGraphics.pose().scale(f, f, 1);
            guiGraphics.pose().translate(-12.5F, -12.5F, 0);
            guiGraphics.blit(RenderType::guiTextured, SYNTHESIZER_LOCATION, 0, 0, slot.getItem().isEmpty() ? 231 : 206, 29, 25, 25, 256, 256);
            guiGraphics.pose().translate(-(slot.x - 4), -(slot.y - 4), 0);
        }
        if (!(slot instanceof SynthesizerMenu.CentrifugeSlot)) {
            super.renderSlot(guiGraphics, slot);
        }
        if (slot.index == 2) {
            guiGraphics.pose().translate(slot.x - 4 - 2, slot.y - 4 - 2, 255);
            guiGraphics.pose().translate(14.5F, 14.5F, 0);
            f = timer.value(ClientUtil.getPartialTick());
            guiGraphics.pose().scale(f, f, 1);
            guiGraphics.pose().translate(-14.5F, -14.5F, 0);
            guiGraphics.blit(RenderType::guiTextured, SYNTHESIZER_LOCATION, 0, 0, 218, 0, 29, 29, 256, 256);
            guiGraphics.pose().translate(0, 0, -255);
            guiGraphics.pose().popPose();
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, this.titleLabelY - 20, -1, false);
        //guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
        //super.renderLabels(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        Component component = getToolTip();

        if (component != null && this.menu.getBurnProgress() == 0) {
            SynthesizerScreen.renderTooltip(guiGraphics, component, mouseX, mouseY, this.leftPos + 139, this.topPos + 3, 29, 18);
            this.startButton.active = false;
        } else {
            this.startButton.active = true;
        }
    }

    private @Nullable Component getToolTip() {
        Component component = null;
        if (this.getMenu().getItems().get(1).isEmpty()) {
            if (!this.getMenu().isLit()) {
                component = Component.translatable("tooltip.theboys.synthesizer.fuel");
            }
        } else if (this.getMenu().getWaterMb() < 250) {
            component = Component.translatable("tooltip.theboys.synthesizer.water", 250 - this.getMenu().getWaterMb());
        } else if (this.getMenu().isCentrifugeEmpty()) {
            component = Component.translatable("tooltip.theboys.synthesizer.centrifugeEmpty");
        } else if (this.getMenu().getItems().get(2).isEmpty()) {
            component = Component.translatable("tooltip.theboys.synthesizer.centerItem");
        } else if (this.getMenu().getItems().get(2).getCount() > 1) {
            component = Component.translatable("tooltip.theboys.synthesizer.oneCenterItem");
        }
        return component;
    }
}
