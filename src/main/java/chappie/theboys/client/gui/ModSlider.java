package chappie.theboys.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.text.DecimalFormat;
import java.util.function.Function;

public class ModSlider extends AbstractSliderButton {

    public final double initialValue;
    protected final Function<ModSlider, Component> messageConsumer;
    protected final DecimalFormat format;
    public Component blockedBy;
    protected int click, tick;
    protected double minValue, maxValue;

    public ModSlider(int x, int y, int width, int height, Function<ModSlider, Component> messageConsumer, double minValue, double maxValue, double currentValue, String decimalFormat) {
        super(x, y, width, height, Component.empty(), 0D);
        this.messageConsumer = messageConsumer;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.format = new DecimalFormat(decimalFormat);
        this.initialValue = currentValue;
        this.valueToInitial();
    }

    public ModSlider(int x, int y, int width, int height, Function<ModSlider, Component> messageConsumer, double minValue, double maxValue, double currentValue) {
        this(x, y, width, height, messageConsumer, minValue, maxValue, currentValue, "0");
    }

    public void tick() {
        if (this.click > 0) {
            if (this.tick > 0) {
                this.tick--;
            } else {
                this.click = 0;
            }
        }


        if (this.click >= 2) {
            this.valueToInitial();
        }
        if (this.isBlocked()) {
            this.setFocused(false);
        }
    }

    @Override
    public void renderWidget(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SLIDER_LOCATION);

        final Minecraft mc = Minecraft.getInstance();
        blitWithBorder(poseStack, this.getX(), this.getY(), 0, getTextureY(), this.width, this.height, 200, 20, 2, 3, 2, 2, 0);

        blitWithBorder(poseStack, this.getX() + (int) (this.value * (double) (this.width - 8)), this.getY(), 0, getHandleTextureY(), 8, this.height, 200, 20, 2, 3, 2, 2, 0);

        if (isBlocked()) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.6F);
            blitWithBorder(poseStack, this.getX(), this.getY(), 0, 40 + (this.isHovered ? 20 : 0), this.width, this.height, 200, 20, 2, 3, 2, 2, 0);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        int i = this.getX() + 2;
        int j = this.getX() + this.getWidth() - 2;
        renderScrollingString(poseStack, mc.font, this.getMessage(), i, this.getY(), j, this.getY() + this.getHeight(), getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
    }


    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.isBlocked()) return;
        super.onClick(mouseX, mouseY);
        this.tick = 5;
        this.click++;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        if (!this.isBlocked()) {
            super.onDrag(mouseX, mouseY, dragX, dragY);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isBlocked()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public Component getMessage() {
        return isBlocked() ? this.blockedBy : super.getMessage();
    }

    public boolean isBlocked() {
        return this.blockedBy != null && !(this.blockedBy.getString().isBlank() || this.blockedBy.getString().isEmpty());
    }

    @Override
    protected int getHandleTextureY() {
        return this.isBlocked() ? 40 : super.getHandleTextureY();
    }

    public double getValue() {
        return Mth.clampedLerp(minValue, this.maxValue(), this.value);
    }

    public String getValueString() {
        return this.format.format(this.getValue());
    }

    public double maxValue() {
        return this.maxValue;
    }

    public void setValue(double value) {
        this.value = (value - minValue) / (maxValue() - minValue);
        this.applyValue();
    }

    public void valueToInitial() {
        this.setValue(this.initialValue);
        this.click = this.tick = 0;
    }

    public void copy(ModSlider slider) {
        if (slider != null) {
            this.value = slider.value;
            this.minValue = slider.minValue;
            this.maxValue = slider.maxValue;
            this.blockedBy = slider.blockedBy;
        }
        this.updateMessage();
    }

    @Override
    public void updateMessage() {
        this.setMessage(this.messageConsumer.apply(this));
    }

    @Override
    protected void applyValue() {
        this.updateMessage();
    }

    public static void blitWithBorder(PoseStack poseStack, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight,
                                      int topBorder, int bottomBorder, int leftBorder, int rightBorder, float zLevel) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int fillerWidth = textureWidth - leftBorder - rightBorder;
        int fillerHeight = textureHeight - topBorder - bottomBorder;
        int canvasWidth = width - leftBorder - rightBorder;
        int canvasHeight = height - topBorder - bottomBorder;
        int xPasses = canvasWidth / fillerWidth;
        int remainderWidth = canvasWidth % fillerWidth;
        int yPasses = canvasHeight / fillerHeight;
        int remainderHeight = canvasHeight % fillerHeight;

        // Draw Border
        // Top Left
        drawTexturedModalRect(poseStack, x, y, u, v, leftBorder, topBorder, zLevel);
        // Top Right
        drawTexturedModalRect(poseStack, x + leftBorder + canvasWidth, y, u + leftBorder + fillerWidth, v, rightBorder, topBorder, zLevel);
        // Bottom Left
        drawTexturedModalRect(poseStack, x, y + topBorder + canvasHeight, u, v + topBorder + fillerHeight, leftBorder, bottomBorder, zLevel);
        // Bottom Right
        drawTexturedModalRect(poseStack, x + leftBorder + canvasWidth, y + topBorder + canvasHeight, u + leftBorder + fillerWidth, v + topBorder + fillerHeight, rightBorder, bottomBorder, zLevel);

        for (int i = 0; i < xPasses + (remainderWidth > 0 ? 1 : 0); i++) {
            // Top Border
            drawTexturedModalRect(poseStack, x + leftBorder + (i * fillerWidth), y, u + leftBorder, v, (i == xPasses ? remainderWidth : fillerWidth), topBorder, zLevel);
            // Bottom Border
            drawTexturedModalRect(poseStack, x + leftBorder + (i * fillerWidth), y + topBorder + canvasHeight, u + leftBorder, v + topBorder + fillerHeight, (i == xPasses ? remainderWidth : fillerWidth), bottomBorder, zLevel);

            // Throw in some filler for good measure
            for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++)
                drawTexturedModalRect(poseStack, x + leftBorder + (i * fillerWidth), y + topBorder + (j * fillerHeight), u + leftBorder, v + topBorder, (i == xPasses ? remainderWidth : fillerWidth), (j == yPasses ? remainderHeight : fillerHeight), zLevel);
        }

        // Side Borders
        for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++) {
            // Left Border
            drawTexturedModalRect(poseStack, x, y + topBorder + (j * fillerHeight), u, v + topBorder, leftBorder, (j == yPasses ? remainderHeight : fillerHeight), zLevel);
            // Right Border
            drawTexturedModalRect(poseStack, x + leftBorder + canvasWidth, y + topBorder + (j * fillerHeight), u + leftBorder + fillerWidth, v + topBorder, rightBorder, (j == yPasses ? remainderHeight : fillerHeight), zLevel);
        }
    }

    public static void drawTexturedModalRect(PoseStack poseStack, int x, int y, int u, int v, int width, int height, float zLevel) {
        final float scale = 1f / 0x100;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder wr = tesselator.getBuilder();
        wr.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f matrix = poseStack.last().pose();
        wr.vertex(matrix, x, y + height, zLevel).uv(u * scale, ((v + height) * scale)).endVertex();
        wr.vertex(matrix, x + width, y + height, zLevel).uv((u + width) * scale, ((v + height) * scale)).endVertex();
        wr.vertex(matrix, x + width, y, zLevel).uv((u + width) * scale, (v * scale)).endVertex();
        wr.vertex(matrix, x, y, zLevel).uv(u * scale, (v * scale)).endVertex();
        tesselator.end();
    }
}