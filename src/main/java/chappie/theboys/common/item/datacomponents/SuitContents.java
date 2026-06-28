package chappie.theboys.common.item.datacomponents;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record SuitContents(ResourceLocation itemId) {
    public static final Codec<SuitContents> CODEC = Codec.either(ItemStack.CODEC, ResourceLocation.CODEC)
            .xmap(value -> value.map(SuitContents::fromStack, SuitContents::new), value -> Either.right(value.itemId));
    public static final StreamCodec<ByteBuf, SuitContents> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(value -> new SuitContents(ResourceLocation.parse(value)), value -> value.itemId().toString());

    public static SuitContents fromStack(ItemStack stack) {
        return new SuitContents(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public ItemStack toStack() {
        return new ItemStack(BuiltInRegistries.ITEM.get(this.itemId));
    }
}
