package chappie.theboys.util.conditional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import org.jetbrains.annotations.Nullable;

public record HasVialDataProperty(int index) implements ConditionalItemModelProperty {
	public static final MapCodec<HasVialDataProperty> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("index", 0).forGetter(HasVialDataProperty::index))
				.apply(instance, HasVialDataProperty::new)
	);

	@Override
	public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
		CustomModelData customModelData = stack.get(DataComponents.CUSTOM_MODEL_DATA); // @TODO
		/*if (pStack.getTag() != null && pStack.getTag().contains("vial")) {
			return 1;
		}*/
		return customModelData != null && customModelData.getBoolean(this.index) == Boolean.TRUE;
	}

	@Override
	public MapCodec<HasVialDataProperty> type() {
		return MAP_CODEC;
	}
}
