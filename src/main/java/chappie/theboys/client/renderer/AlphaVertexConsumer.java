package chappie.theboys.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public record AlphaVertexConsumer(VertexConsumer delegate, float alpha) implements VertexConsumer {

    public AlphaVertexConsumer(VertexConsumer delegate, float alpha) {
        this.delegate = delegate;
        this.alpha = Mth.clamp(alpha, 0.0F, 1.0F);
    }

    private int scaleAlpha(int a) {
        return Mth.clamp((int) (a * this.alpha), 0, 255);
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        this.delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        this.delegate.setColor(red, green, blue, scaleAlpha(alpha));
        return this;
    }

    @Override
    public VertexConsumer setColor(float red, float green, float blue, float alpha) {
        this.delegate.setColor(red, green, blue, alpha * this.alpha);
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        int a = FastColor.ARGB32.alpha(color);
        int r = FastColor.ARGB32.red(color);
        int g = FastColor.ARGB32.green(color);
        int b = FastColor.ARGB32.blue(color);
        this.delegate.setColor(r, g, b, scaleAlpha(a));
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        this.delegate.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        this.delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        this.delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        this.delegate.setNormal(x, y, z);
        return this;
    }
}
