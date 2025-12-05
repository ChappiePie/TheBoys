package chappie.theboys.common.particle;

import chappie.theboys.TheBoys;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class TBParticleTypes {

    public static final ParticleType<LaserParticleOptions> LASER = Registry.register(BuiltInRegistries.PARTICLE_TYPE, TheBoys.id("laser"), complex(false));

    public static ParticleType<LaserParticleOptions> complex(boolean alwaysSpawn) {
        return new ParticleType<>(alwaysSpawn) {
            @Override
            public MapCodec<LaserParticleOptions> codec() {
                return LaserParticleOptions.CODEC;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, LaserParticleOptions> streamCodec() {
                return LaserParticleOptions.STREAM_CODEC;
            }
        };
    }

    public static final SimpleParticleType WATER_SPLASH = Registry.register(BuiltInRegistries.PARTICLE_TYPE, TheBoys.id("water_splash"), FabricParticleTypes.simple());

    public static void init() {

    }
}
