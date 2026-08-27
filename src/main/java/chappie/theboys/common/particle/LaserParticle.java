package chappie.theboys.common.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.RisingParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

public class LaserParticle extends RisingParticle {

    private final float rot, rotO;
    private final float pitch, pitchO;

    public LaserParticle(int color, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, float pitch, float rot, TextureAtlasSprite sprite) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, sprite);
        this.lifetime = 20;
        this.pitchO = this.pitch = pitch;
        this.rotO = this.rot = rot;
        this.rCol = ARGB.redFloat(color);
        this.gCol = ARGB.greenFloat(color);
        this.bCol = ARGB.blueFloat(color);
    }

    @Override
    public void tick() {
        this.alpha = 1.0F - this.age / (float) this.lifetime;
        super.tick();
    }

    public void move(double pX, double pY, double pZ) {
        this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
        this.setLocationFromBoundingbox();
    }

    public float getQuadSize(float pScaleFactor) {
        float f = ((float) this.age + pScaleFactor) / (float) this.lifetime;
        return this.quadSize * (1.0F - f * f * 0.5F);
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return 240 | (240 << 16); // Full brightness
    }

    @Override
    public void extract(QuadParticleRenderState reusedState, Camera camera, float partialTick) {
        float interpRot = -Mth.lerp(partialTick, this.rotO, this.rot) * ((float) Math.PI / 180F);
        float interpPitch = Mth.lerp(partialTick, this.pitchO, this.pitch) * ((float) Math.PI / 180F);
        Quaternionf facing = new Quaternionf().rotationY(interpRot).rotateX(interpPitch);
        this.extractRotatedQuad(reusedState, camera, facing, partialTick);
        Quaternionf mirrored = new Quaternionf(facing).rotateY((float) Math.PI);
        this.extractRotatedQuad(reusedState, camera, mirrored, partialTick);
    }
}