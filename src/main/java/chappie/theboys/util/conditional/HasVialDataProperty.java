package chappie.theboys.util.conditional;

import chappie.theboys.common.item.datacomponents.TBDataComponents;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record HasVialDataProperty(int index) implements ConditionalItemModelProperty {
	public static final MapCodec<HasVialDataProperty> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("index", 0).forGetter(HasVialDataProperty::index))
				.apply(instance, HasVialDataProperty::new)
	);

	@Override
	public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
		return !stack.getOrDefault(TBDataComponents.VIAL, ItemStack.EMPTY).isEmpty();
	}

	@Override
	public MapCodec<HasVialDataProperty> type() {
		return MAP_CODEC;
	}
}
