package chappie.theboys.common.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.RisingParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
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
    public void extract(QuadParticleRenderState renderState, Camera camera, float partialTick) {
        Vec3 cameraPos = camera.getPosition();
        float interpRot = -Mth.lerp(partialTick, this.rotO, this.rot) * ((float) Math.PI / 180F);
        float interpPitch = (float) (Mth.lerp(partialTick, this.pitchO, this.pitch) + Math.PI / 2F) * ((float) Math.PI / 180F);
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z());

        Vec3 offset = new Vec3(x, y - 0.25F, z).add(new Vec3(0.0, 0.3, 0.0).yRot(interpRot));
        float quadSize = this.getQuadSize(partialTick);
        Quaternionf orientation = new Quaternionf().rotationYXZ(interpRot, interpPitch, 0.0F);

        renderState.add(
                this.getLayer(),
                (float) offset.x,
                (float) offset.y,
                (float) offset.z,
                orientation.x,
                orientation.y,
                orientation.z,
                orientation.w,
                quadSize,
                this.getU0(),
                this.getU1(),
                this.getV0(),
                this.getV1(),
                ARGB.colorFromFloat(this.alpha * 0.5F, this.rCol, this.gCol, this.bCol),
                this.getLightColor(partialTick)
        );
    }

    @Environment(EnvType.CLIENT)
    public record LaserParticleFactory(SpriteSet sprite) implements ParticleProvider<LaserParticleOptions> {

        @Override
        public Particle createParticle(LaserParticleOptions pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, RandomSource random) {
            float rot = 0, pitch = 0;
            if (pLevel.getEntity(pType.entityId()) instanceof LivingEntity e) {
                rot = e.getYRot();
                pitch = Math.min(e.getXRot(), 45);
            }
            TextureAtlasSprite sprite = this.sprite.get(pLevel.random);
            LaserParticle particle = new LaserParticle(pType.color(), pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, pitch, rot, sprite);
            particle.scale(2F);

            return particle;
        }
    }

    public record LaserParticleOptions(int entityId, int color) implements ParticleOptions {
        public static final MapCodec<LaserParticleOptions> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(Codec.INT.fieldOf("entityId").forGetter(optionsBase -> optionsBase.entityId),
                                Codec.INT.fieldOf("color").forGetter(optionsBase -> optionsBase.color))
                        .apply(instance, LaserParticleOptions::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, LaserParticleOptions> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, (opt) -> opt.entityId, ByteBufCodecs.VAR_INT, (opt) -> opt.color, LaserParticleOptions::new
        );

        @Override
        public @NotNull ParticleType<?> getType() {
            return TBParticleTypes.LASER;
        }
    }
}
