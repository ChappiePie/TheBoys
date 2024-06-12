package chappie.theboys.common.particle;

import chappie.theboys.TheBoys;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TBParticleTypes {

    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, TheBoys.MODID);

    public static final RegistryObject<ParticleType<LaserParticle.LaserParticleOptions>> LASER = PARTICLES.register("laser", () -> new ParticleType<>(false, LaserParticle.LaserParticleOptions.DESERIALIZER) {
        public Codec<LaserParticle.LaserParticleOptions> codec() {
            return LaserParticle.LaserParticleOptions.CODEC;
        }
    });
}
