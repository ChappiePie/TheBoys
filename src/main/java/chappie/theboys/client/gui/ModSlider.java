package chappie.theboys.client.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

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

    // Rendering handled by AbstractSliderButton.extractWidgetRenderState()
    // Custom message via getMessage() override handles blocked state display


    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        if (this.isBlocked()) return;
        this.tick = 5;
        this.click++;
        super.onClick(event, isDoubleClick);
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        if (this.isBlocked()) return;
        super.onDrag(event, dx, dy);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!this.isBlocked()) {
            return super.keyPressed(event);
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
        return Mth.clampedLerp(this.value, minValue, this.maxValue());
    }

    public void setRealValue(double value) {
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
        this.setRealValue(this.initialValue);
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