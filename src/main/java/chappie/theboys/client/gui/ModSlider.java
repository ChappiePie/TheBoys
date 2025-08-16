package chappie.theboys.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

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
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        final Minecraft mc = Minecraft.getInstance();
        guiGraphics.blitSprite(RenderType::guiTextured, this.getSprite(), this.getX(), this.getY(), this.getWidth(), this.getHeight());
        guiGraphics.blitSprite(RenderType::guiTextured, this.getHandleSprite(), this.getX() + (int) (this.value * (double) (this.width - 8)), this.getY(), 8, this.getHeight());

        if (isBlocked()) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.6F);
            guiGraphics.blitSprite(RenderType::guiTextured, this.getHandleSprite(), this.getX() + (int) (this.value * (double) (this.width - 8)), this.getY(), 8, this.getHeight());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        int i = this.getX() + 2;
        int j = this.getX() + this.getWidth() - 2;
        renderScrollingString(guiGraphics, mc.font, this.getMessage(), i, this.getY(), j, this.getY() + this.getHeight(), (this.active ? 16777215 : 10526880) | Mth.ceil(this.alpha * 255.0F) << 24);
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

    public double getValue() {
        return Mth.clampedLerp(minValue, this.maxValue(), this.value);
    }

    public void setValue(double value) {
        this.value = (value - minValue) / (maxValue() - minValue);
        this.applyValue();
    }

    public String getValueString() {
        return this.format.format(this.getValue());
    }

    public double maxValue() {
        return this.maxValue;
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
}