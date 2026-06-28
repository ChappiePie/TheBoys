package chappie.theboys.common.item.datacomponents;

import chappie.theboys.TheBoys;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class TBDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
        DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, TheBoys.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SUPERPOWER = register(
            "superpower", builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<VialContents>> VIAL = register(
            "vial", builder -> builder.persistent(VialContents.CODEC).networkSynchronized(VialContents.STREAM_CODEC)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SuitContents>> SUIT = register(
            "suit", builder -> builder.persistent(SuitContents.CODEC).networkSynchronized(SuitContents.STREAM_CODEC)
    );

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return DATA_COMPONENT_TYPES.register(name, () -> builder.apply(DataComponentType.builder()).build());
    }

    public static void init(IEventBus modEventBus) {
        DATA_COMPONENT_TYPES.register(modEventBus);
    }
}
