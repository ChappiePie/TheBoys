package chappie.theboys.common.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public record LaserParticleFactory(SpriteSet sprite) implements ParticleProvider<LaserParticleOptions> {

    @Override
    public Particle createParticle(LaserParticleOptions pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, RandomSource random) {
        float rot = 0, pitch = 0;
        if (pLevel.getEntity(pType.entityId()) instanceof LivingEntity e) {
            rot = e.getYRot();
            pitch = Math.min(e.getXRot(), 45);
        }
        TextureAtlasSprite sprite = this.sprite.get(pLevel.getRandom());
        LaserParticle particle = new LaserParticle(pType.color(), pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, pitch, rot, sprite);
        particle.scale(2F);

        return particle;
    }
}