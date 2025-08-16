package chappie.theboys.client.item;

import chappie.theboys.common.item.SyringeItem;
import chappie.theboys.common.item.VialItem;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record VialTintSource(int defaultColor) implements ItemTintSource {
    public static final MapCodec<VialTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(VialTintSource::defaultColor)).apply(instance, VialTintSource::new)
    );

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        if (stack.getItem() instanceof SyringeItem) {
            return SyringeItem.getColor(stack);
        }
        if (stack.getItem() instanceof VialItem) {
            return VialItem.getColor(stack);
        }
        return this.defaultColor;
    }

    @Override
    public MapCodec<VialTintSource> type() {
        return MAP_CODEC;
    }
}

