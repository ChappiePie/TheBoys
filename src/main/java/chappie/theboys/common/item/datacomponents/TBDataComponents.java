package chappie.theboys.common.item.datacomponents;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;

import java.util.function.UnaryOperator;

public class TBDataComponents {

    public static final DataComponentType<String> SUPERPOWER = register(
            "superpower", builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    public static final DataComponentType<ItemStack> VIAL = register(
            "vial", builder -> builder.persistent(ItemStack.CODEC).networkSynchronized(ItemStack.STREAM_CODEC)
    );

    public static final DataComponentType<ItemStack> SUIT = register(
            "suit", builder -> builder.persistent(ItemStack.CODEC).networkSynchronized(ItemStack.STREAM_CODEC)
    );

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, name, builder.apply(DataComponentType.builder()).build());
    }

    public static void init() {

    }
}
