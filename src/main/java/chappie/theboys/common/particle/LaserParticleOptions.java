package chappie.theboys.common.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

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
        return TBParticleTypes.LASER.get();
    }
}
