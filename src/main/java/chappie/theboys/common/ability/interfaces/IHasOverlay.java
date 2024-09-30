package chappie.theboys.common.ability.interfaces;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityClientProperties;
import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.common.ability.base.condition.KeyCondition;
import chappie.modulus.util.KeyMap;
import chappie.theboys.client.TBOverlays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class IHasOverlay implements AbilityClientProperties {

    private final Ability ability;
    private Supplier<Integer> uOffset = () -> 16, vOffset = () -> 0;
    private Supplier<Integer> backgroundColor = () -> -1;
    private Supplier<KeyMap.KeyType> keyType = null;

    public IHasOverlay(Ability ability, Consumer<IHasOverlay> builder) {
        this.ability = ability;
        builder.accept(this);
        this.ability.clientProperties.add(this);

    }

    public static IHasOverlay getInstance(Ability ability) {
        for (AbilityClientProperties clientProperty : ability.clientProperties) {
            if (clientProperty instanceof IHasOverlay overlay && !ability.isHidden()) {
                return overlay;
            }
        }
        return null;
    }

    public IHasOverlay uvOffset(Supplier<Integer> u, Supplier<Integer> v) {
        this.uOffset = u;
        this.vOffset = v;
        return this;
    }

    public IHasOverlay uOffset(int u) {
        return uvOffset(() -> u, () -> 0);
    }

    public IHasOverlay keyType(Supplier<KeyMap.KeyType> keyType) {
        this.keyType = keyType;
        return this;
    }

    public IHasOverlay backgroundColor(Supplier<Integer> backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public int getBackgroundColor() {
        return this.backgroundColor.get();
    }

    public KeyMap.KeyType getKeyType() {
        if (this.keyType == null) {
            for (Condition enabling : ability.conditionManager.conditionsFor("enabling")) {
                if (enabling instanceof KeyCondition keyCondition) {
                    return keyCondition.keyType;
                }
            }
            return null;
        } else {
            return this.keyType.get();
        }
    }

    public void renderIcon(int x, int y, float alpha, Minecraft mc, Gui gui, GuiGraphics guiGraphics, float partialTick, int width, int height) {
        guiGraphics.blit(TBOverlays.TEXTURE, x, y, this.uOffset.get(), this.vOffset.get(), 16, 16, 256, 256);
    }
}
