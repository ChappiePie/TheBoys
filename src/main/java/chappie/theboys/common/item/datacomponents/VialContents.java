package chappie.theboys.common.item.datacomponents;

import chappie.theboys.common.item.TBItems;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record VialContents(String superpower) {
    public static final VialContents EMPTY = new VialContents("");
    public static final Codec<VialContents> CODEC = Codec.either(ItemStack.CODEC, Codec.STRING)
            .xmap(value -> value.map(VialContents::fromStack, VialContents::new), value -> Either.right(value.superpower));
    public static final StreamCodec<ByteBuf, VialContents> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(VialContents::new, VialContents::superpower);

    public static VialContents fromStack(ItemStack stack) {
        return new VialContents(stack.getOrDefault(TBDataComponents.SUPERPOWER, ""));
    }

    public ItemStack toStack() {
        ItemStack stack = TBItems.VIAL.getDefaultInstance();
        if (!this.superpower.isBlank()) {
            stack.set(TBDataComponents.SUPERPOWER, this.superpower);
        }
        return stack;
    }

    public boolean hasSuperpower() {
        return !this.superpower.isBlank();
    }
}
