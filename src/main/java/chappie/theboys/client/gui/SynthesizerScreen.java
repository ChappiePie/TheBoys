package chappie.theboys.client.gui;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.IHasTimer;
import chappie.theboys.TheBoys;
import chappie.theboys.client.gui.buttons.SynthesizerStartButton;
import chappie.theboys.common.block.entity.SynthesizerBlockEntity;
import chappie.theboys.common.block.menu.SynthesizerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import java.util.List;

public class SynthesizerScreen extends AbstractContainerScreen<SynthesizerMenu> {
    private static final Identifier SYNTHESIZER_LOCATION = TheBoys.id("textures/gui/synthesizer.png");
    public static IHasTimer.Timer timer = new IHasTimer.Timer(() -> 10, () -> false);
    public static IHasTimer.Timer rollTimer = new SynthesizerBlockEntity.RollTimer(() -> false);

    private SynthesizerStartButton startButton;

    public SynthesizerScreen(SynthesizerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    public static void renderTooltip(GuiGraphicsExtractor GuiGraphicsExtractor, Component toolTip, int pMouseX, int pMouseY, int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        boolean isMouseOverObj = pMouseX >= x && pMouseY >= y && pMouseX <= x + width && pMouseY <= y + height;
        if (isMouseOverObj) {
            GuiGraphicsExtractor.setComponentTooltipForNextFrame(mc.font, List.of(toolTip), pMouseX, pMouseY);
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
    public void extractBackground(GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(GuiGraphicsExtractor, mouseX, mouseY, partialTick);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, SYNTHESIZER_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        float f = this.getMenu().data.get(1) / 500F;

        int progress = (int) (37 * f);
        GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, SYNTHESIZER_LOCATION, i + 16, j + 10 + 37 - progress, 190, 37 - progress, 12, progress, 256, 256);

        int progress1 = Mth.ceil(this.menu.getBurnProgress() * 16.0F);
        GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, SYNTHESIZER_LOCATION, i + 145, j + 28, 202, 0, progress1, 5, 256, 256);


        int progress2 = Mth.ceil(this.menu.getLitProgress() * 14.0F);
        GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, SYNTHESIZER_LOCATION, i + 146, j + 37 + 14 - progress2, 176, 14 - progress2, 14, progress2, 256, 256);


        Matrix3x2fStack matrix = GuiGraphicsExtractor.pose();
        matrix.pushMatrix();
        matrix.translate(i, j);
        matrix.pushMatrix();
        int scissorLeft = 36;
        int scissorTop = 0;
        int scissorRight = scissorLeft + 103;
        int scissorBottom = scissorTop + 81;
        GuiGraphicsExtractor.enableScissor(scissorLeft, scissorTop, scissorRight, scissorBottom);

        matrix.translate(87.5F, 40.5F);
        matrix.rotate((float) Math.toRadians(-360F * rollTimer.value(ClientUtil.getPartialTick())));
        matrix.translate(-87.5F, -40.5F);

        for (Slot slot : this.menu.slots) {
            if (slot instanceof SynthesizerMenu.CentrifugeSlot) {
                GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, SYNTHESIZER_LOCATION, slot.x - 4, slot.y - 4, 218, 54, 24, 24, 256, 256);
                this.extractSlot(GuiGraphicsExtractor, slot, mouseX, mouseY);
            }
        }
        GuiGraphicsExtractor.disableScissor();
        matrix.popMatrix();

        Slot mainSlot = this.menu.slots.get(2);
        if (!mainSlot.isActive()) {
            this.extractSlot(GuiGraphicsExtractor, mainSlot, mouseX, mouseY);
        }
        matrix.popMatrix();
    }

    @Override
    protected void extractSlot(@NotNull GuiGraphicsExtractor GuiGraphicsExtractor, Slot slot, int mouseX, int mouseY) {
        float f = Math.max(0.4F, 1.0F - timer.value(ClientUtil.getPartialTick()));
        if (slot.index == 2) {
            Matrix3x2fStack matrix = GuiGraphicsExtractor.pose();
            matrix.pushMatrix();
            matrix.translate(slot.x - 4F, slot.y - 4F);
            matrix.translate(12.5F, 12.5F);
            matrix.scale(f, f);
            matrix.translate(-12.5F, -12.5F);
            GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, SYNTHESIZER_LOCATION, 0, 0, slot.getItem().isEmpty() ? 231 : 206, 29, 25, 25, 256, 256);
            matrix.popMatrix();
        }
        if (!(slot instanceof SynthesizerMenu.CentrifugeSlot)) {
            super.extractSlot(GuiGraphicsExtractor, slot, mouseX, mouseY);
        }
        if (slot.index == 2) {
            Matrix3x2fStack matrix = GuiGraphicsExtractor.pose();
            matrix.pushMatrix();
            matrix.translate(slot.x - 6F, slot.y - 6F);
            matrix.translate(14.5F, 14.5F);
            float scale = timer.value(ClientUtil.getPartialTick());
            matrix.scale(scale, scale);
            matrix.translate(-14.5F, -14.5F);
            GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, SYNTHESIZER_LOCATION, 0, 0, 218, 0, 29, 29, 256, 256);
            matrix.popMatrix();
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY) {
        GuiGraphicsExtractor.text(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, this.titleLabelY - 20, -1, false);
        //GuiGraphicsExtractor.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
        //super.renderLabels(GuiGraphicsExtractor, mouseX, mouseY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(GuiGraphicsExtractor, mouseX, mouseY, partialTick);

        Component component = getToolTip();

        if (component != null && this.menu.getBurnProgress() == 0) {
            SynthesizerScreen.renderTooltip(GuiGraphicsExtractor, component, mouseX, mouseY, this.leftPos + 139, this.topPos + 3, 29, 18);
            this.startButton.active = false;
        } else {
            this.startButton.active = true;
        }
    }

    private @Nullable Component getToolTip() {
        Component component = null;
        if (this.getMenu().getItems().get(1).isEmpty() && !this.getMenu().isLit()) {
            component = Component.translatable("tooltip.theboys.synthesizer.fuel");
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