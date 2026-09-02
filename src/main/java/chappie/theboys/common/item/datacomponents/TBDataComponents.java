package chappie.theboys.common.item.datacomponents;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.function.UnaryOperator;

public class TBDataComponents {

    public static final DataComponentType<String> SUPERPOWER = register(
            "superpower", builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    public static final DataComponentType<VialContents> VIAL = register(
            "vial", builder -> builder.persistent(VialContents.CODEC).networkSynchronized(VialContents.STREAM_CODEC)
    );

    public static final DataComponentType<SuitContents> SUIT = register(
            "suit", builder -> builder.persistent(SuitContents.CODEC).networkSynchronized(SuitContents.STREAM_CODEC)
    );

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, name, builder.apply(DataComponentType.builder()).build());
    }

    public static void init() {

    }
}
