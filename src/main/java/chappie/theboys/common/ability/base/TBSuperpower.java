package chappie.theboys.common.ability.base;

import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.Superpower;
import chappie.theboys.client.TBOverlays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;

import java.util.LinkedList;

public class TBSuperpower extends Superpower {

    private int uOffset = 0, vOffset = 128;

    public TBSuperpower(LinkedList<AbilityBuilder> list) {
        super(list);
    }

    public TBSuperpower(AbilityBuilder... builders) {
        super(builders);
    }

    public TBSuperpower uvOffset(int u, int v) {
        this.uOffset = u;
        this.vOffset = v;
        return this;
    }

    public TBSuperpower uOffset(int u) {
        this.uOffset = u;
        return this;
    }

    public void renderIcon(int x, int y, float alpha, Minecraft mc, GuiGraphics guiGraphics, float partialTick) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TBOverlays.TEXTURE, x, y, this.uOffset, this.vOffset, 16, 16, 256, 256, ARGB.white(alpha));
    }
}
