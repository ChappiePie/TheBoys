package chappie.theboys.common.particle;

import chappie.theboys.TheBoys;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public class TBParticleTypes {

    public static final ParticleType<LaserParticle.LaserParticleOptions> LASER = Registry.register(BuiltInRegistries.PARTICLE_TYPE, TheBoys.id("laser"), complex(false, LaserParticle.LaserParticleOptions.DESERIALIZER));

    public static ParticleType<LaserParticle.LaserParticleOptions> complex(boolean alwaysSpawn, ParticleOptions.Deserializer<LaserParticle.LaserParticleOptions> factory) {
        return new ParticleType<>(alwaysSpawn, factory) {
            @Override
            public Codec<LaserParticle.LaserParticleOptions> codec() {
                return LaserParticle.LaserParticleOptions.CODEC;
            }
        };
    }

    public static void init() {

    }
}
