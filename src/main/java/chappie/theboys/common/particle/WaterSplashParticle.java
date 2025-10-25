package chappie.theboys.common.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class WaterSplashParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public WaterSplashParticle(ClientLevel clientLevel, double d, double e, double f, SpriteSet sprites) {
        super(clientLevel, d, e, f);
        this.lifetime = 11;
        this.hasPhysics = false;
        this.gravity = -0.1F;
        this.quadSize = 1F;
        this.sprites = sprites;
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.sprites);
            this.yd = this.yd - 0.04 * (double)this.gravity;
            this.move(this.xd, this.yd, this.zd);
            if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo) {
                this.xd *= 1.1;
                this.zd *= 1.1;
            }

            this.xd = this.xd * (double)this.friction;
            this.yd = this.yd * (double)this.friction;
            this.zd = this.zd * (double)this.friction;
            if (this.onGround) {
                this.xd *= 0.7F;
                this.zd *= 0.7F;
            }
        }
    }


    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Provider(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                return new WaterSplashParticle(level, x, y, z, this.sprite);
            }
        }
}
