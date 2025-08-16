package chappie.theboys.common.particle;

import chappie.theboys.TheBoys;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class TBParticleTypes {

    public static final ParticleType<LaserParticle.LaserParticleOptions> LASER = Registry.register(BuiltInRegistries.PARTICLE_TYPE, TheBoys.id("laser"), complex(false));

    public static ParticleType<LaserParticle.LaserParticleOptions> complex(boolean alwaysSpawn) {
        return new ParticleType<>(alwaysSpawn) {
            @Override
            public MapCodec<LaserParticle.LaserParticleOptions> codec() {
                return LaserParticle.LaserParticleOptions.CODEC;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, LaserParticle.LaserParticleOptions> streamCodec() {
                return LaserParticle.LaserParticleOptions.STREAM_CODEC;
            }
        };
    }

    public static void init() {

    }
}
