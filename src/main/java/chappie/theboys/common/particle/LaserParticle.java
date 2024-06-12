package chappie.theboys.common.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Locale;

public class LaserParticle extends RisingParticle {

    private final float rot, rotO;
    private final float pitch, pitchO;

    public LaserParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, float pitch, float rot) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.lifetime = 20;
        this.pitchO = this.pitch = pitch;
        this.rotO = this.rot = rot;
    }

    @Override
    public void tick() {
        this.alpha = 1.0F - this.age / (float)this.lifetime;
        super.tick();
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public void move(double pX, double pY, double pZ) {
        this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
        this.setLocationFromBoundingbox();
    }

    public float getQuadSize(float pScaleFactor) {
        float f = ((float)this.age + pScaleFactor) / (float)this.lifetime;
        return this.quadSize * (1.0F - f * f * 0.5F);
    }

    public int getLightColor(float pPartialTick) {
        float f = ((float)this.age + pPartialTick) / (float)this.lifetime;
        f = Mth.clamp(f, 0.0F, 1.0F);
        int i = super.getLightColor(pPartialTick);
        int j = i & 255;
        int k = i >> 16 & 255;
        j += (int)(f * 15.0F * 16.0F);
        if (j > 240) {
            j = 240;
        }

        return j | k << 16;
    }

    @Override
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        Vec3 vec3 = pRenderInfo.getPosition();
        float rot = -Mth.lerp(pPartialTicks, this.rotO, this.rot) * ((float)Math.PI / 180F);
        float pitch = (float) (Mth.lerp(pPartialTicks, this.pitchO, this.pitch) + Math.PI / 2F) * ((float)Math.PI / 180F);
        float x = (float)(Mth.lerp(pPartialTicks, this.xo, this.x) - vec3.x());
        float y = (float)(Mth.lerp(pPartialTicks, this.yo, this.y) - vec3.y());
        float z = (float)(Mth.lerp(pPartialTicks, this.zo, this.z) - vec3.z());
        Vector3f vec3f = (new Vector3f(0.5F, 0.5F, 0.5F)).normalize();
        Quaternionf quaternionf = (new Quaternionf()).setAngleAxis(0.0F, vec3f.x(), vec3f.y(), vec3f.z());
        quaternionf.rotationYXZ(rot, pitch, 0.0F);

        quaternionf.transform(new Vector3f(-1.0F, -1.0F, 0.0F));
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f3 = this.getQuadSize(pPartialTicks);

        vec3 = new Vec3(x, y - 0.25F, z).add(new Vec3(0, 0.3, 0).yRot(rot));
        for (Vector3f vector3f : avector3f) {
            vector3f.rotate(quaternionf);
            vector3f.mul(f3);
            vector3f.add((float) vec3.x, (float) vec3.y, (float) vec3.z);
        }
        int j = this.getLightColor(pPartialTicks);

        var b = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderType renderType = RenderType.entityTranslucentEmissive(TextureAtlas.LOCATION_PARTICLES);
        pBuffer = b.getBuffer(renderType);
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        this.makeCornerVertex(pBuffer, avector3f[0], this.getU1(), this.getV1(), j);
        this.makeCornerVertex(pBuffer, avector3f[1], this.getU1(), this.getV0(), j);
        this.makeCornerVertex(pBuffer, avector3f[2], this.getU0(), this.getV0(), j);
        this.makeCornerVertex(pBuffer, avector3f[3], this.getU0(), this.getV1(), j);
        b.endBatch(renderType);
    }

    private void makeCornerVertex(VertexConsumer pConsumer, Vector3f pVertex, float pU, float pV, int pPackedLight) {
        pConsumer.vertex(pVertex.x(), pVertex.y(), pVertex.z(), this.rCol, this.gCol, this.bCol, this.alpha, pU, pV, OverlayTexture.NO_OVERLAY, pPackedLight, 0.0F, 1.0F, 0.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public static class LaserParticleFactory implements ParticleProvider<LaserParticleOptions> {
        private final SpriteSet sprite;

        public LaserParticleFactory(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        @Override
        public Particle createParticle(LaserParticleOptions pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            float rot = 0, pitch = 0;
            if (pLevel.getEntity(pType.entityId()) instanceof LivingEntity e) {
                rot = e.getYRot();
                pitch = Math.min(e.getXRot(), 45);
            }
            LaserParticle particle = new LaserParticle(pLevel, pX, pY + 0.25F, pZ, pXSpeed, pYSpeed, pZSpeed, pitch, rot);
            particle.pickSprite(this.sprite);
            particle.scale(2F);

            return particle;
        }
    }

    public record LaserParticleOptions(int entityId) implements ParticleOptions {
        public static final Codec<LaserParticleOptions> CODEC = RecordCodecBuilder.create((p_175793_) ->
                p_175793_.group(Codec.INT.fieldOf("entityId")
                                .forGetter((optionsBase) -> optionsBase.entityId))
                        .apply(p_175793_, LaserParticleOptions::new));
        public static final Deserializer<LaserParticleOptions> DESERIALIZER = new Deserializer<>() {
            public LaserParticleOptions fromCommand(ParticleType<LaserParticleOptions> p_123689_, StringReader p_123690_) throws CommandSyntaxException {
                p_123690_.expect(' ');
                int i = p_123690_.readInt();
                p_123690_.expect(' ');
                return new LaserParticleOptions(i);
            }

            public LaserParticleOptions fromNetwork(ParticleType<LaserParticleOptions> p_123692_, FriendlyByteBuf p_123693_) {
                return new LaserParticleOptions(p_123693_.readInt());
            }
        };

        @Override
        public ParticleType<?> getType() {
            return TBParticleTypes.LASER.get();
        }

        public void writeToNetwork(FriendlyByteBuf pBuffer) {
            pBuffer.writeInt(this.entityId);
        }

        public String writeToString() {
            return String.format(Locale.ROOT, "%s %s", ForgeRegistries.PARTICLE_TYPES.getKey(this.getType()), entityId);
        }
    }
}
