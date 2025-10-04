package chappie.theboys.common.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class LaserParticle extends RisingParticle {

    private final float rot, rotO;
    private final float pitch, pitchO;

    public LaserParticle(int color, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, float pitch, float rot) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
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

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
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
    public void renderCustom(PoseStack poseStack, MultiBufferSource bufferSource, Camera pRenderInfo, float pPartialTicks) {
        Vec3 vec3 = pRenderInfo.getPosition();
        float rot = -Mth.lerp(pPartialTicks, this.rotO, this.rot) * ((float) Math.PI / 180F);
        float pitch = (float) (Mth.lerp(pPartialTicks, this.pitchO, this.pitch) + Math.PI / 2F) * ((float) Math.PI / 180F);
        float x = (float) (Mth.lerp(pPartialTicks, this.xo, this.x) - vec3.x());
        float y = (float) (Mth.lerp(pPartialTicks, this.yo, this.y) - vec3.y());
        float z = (float) (Mth.lerp(pPartialTicks, this.zo, this.z) - vec3.z());
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

        RenderType renderType = RenderType.entityTranslucentEmissive(TextureAtlas.LOCATION_PARTICLES);
        VertexConsumer pBuffer = bufferSource.getBuffer(renderType);
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        this.makeCornerVertex(pBuffer, avector3f[0], this.getU1(), this.getV1(), j);
        this.makeCornerVertex(pBuffer, avector3f[1], this.getU1(), this.getV0(), j);
        this.makeCornerVertex(pBuffer, avector3f[2], this.getU0(), this.getV0(), j);
        this.makeCornerVertex(pBuffer, avector3f[3], this.getU0(), this.getV1(), j);
    }

    private void makeCornerVertex(VertexConsumer pConsumer, Vector3f pVertex, float pU, float pV, int pPackedLight) {
        pConsumer.addVertex(pVertex.x(), pVertex.y(), pVertex.z()).setColor(this.rCol, this.gCol, this.bCol, this.alpha * 0.5F).setUv(pU, pV)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(pPackedLight).setNormal(0, 1, 0);
    }

    @Environment(EnvType.CLIENT)
    public record LaserParticleFactory(SpriteSet sprite) implements ParticleProvider<LaserParticleOptions> {

        @Override
        public Particle createParticle(LaserParticleOptions pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            float rot = 0, pitch = 0;
            if (pLevel.getEntity(pType.entityId()) instanceof LivingEntity e) {
                rot = e.getYRot();
                pitch = Math.min(e.getXRot(), 45);
            }
            LaserParticle particle = new LaserParticle(pType.color(), pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, pitch, rot);
            particle.pickSprite(this.sprite);
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
