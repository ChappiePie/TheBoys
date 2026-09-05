package chappie.theboys.common.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.RisingParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class LaserParticle extends RisingParticle {

    private final float rot, rotO;
    private final float pitch, pitchO;

    public LaserParticle(int color, ClientLevel pLevel, double pX, double pY, double pZ,
                         double pXSpeed, double pYSpeed, double pZSpeed,
                         float pitch, float rot, TextureAtlasSprite sprite) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.setSprite(sprite);
        this.lifetime = 20;
        this.pitchO = this.pitch = pitch;
        this.rotO = this.rot = rot;
        this.rCol = ((color >> 16) & 0xFF) / 255.0F;
        this.gCol = ((color >> 8) & 0xFF) / 255.0F;
        this.bCol = (color & 0xFF) / 255.0F;
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

    @Override
    public float getQuadSize(float pScaleFactor) {
        float f = ((float) this.age + pScaleFactor) / (float) this.lifetime;
        return this.quadSize * (1.0F - f * f * 0.5F);
    }

    @Override
    public int getLightColor(float partialTick) {
        float f = ((float) this.age + partialTick) / (float) this.lifetime;
        f = Mth.clamp(f, 0.0F, 1.0F);
        int i = super.getLightColor(partialTick);
        int j = i & 0xFF;
        int k = i >> 16 & 0xFF;
        j += (int) (f * 15.0F * 16.0F);
        if (j > 240) {
            j = 240;
        }
        return j | k << 16;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        float interpRot = -Mth.lerp(partialTick, this.rotO, this.rot) * ((float) Math.PI / 180F);
        float interpPitch = Mth.lerp(partialTick, this.pitchO, this.pitch) * ((float) Math.PI / 180F);
        Quaternionf facing = new Quaternionf().rotationY(interpRot).rotateX(interpPitch);
        this.renderRotatedQuad(buffer, camera, facing, partialTick);
        Quaternionf mirrored = new Quaternionf(facing).rotateY((float) Math.PI);
        this.renderRotatedQuad(buffer, camera, mirrored, partialTick);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
