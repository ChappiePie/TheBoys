package chappie.theboys.common.particle;

import chappie.theboys.TheBoys;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TBParticleTypes {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, TheBoys.MODID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<LaserParticleOptions>> LASER = PARTICLE_TYPES.register("laser",
            () -> new ParticleType<>(false) {
                @Override
                public MapCodec<LaserParticleOptions> codec() {
                    return LaserParticleOptions.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, LaserParticleOptions> streamCodec() {
                    return LaserParticleOptions.STREAM_CODEC;
                }
            });

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> WATER_SPLASH = PARTICLE_TYPES.register("water_splash",
            () -> new SimpleParticleType(false));

    public static void init(IEventBus modEventBus) {
        PARTICLE_TYPES.register(modEventBus);
    }
}
