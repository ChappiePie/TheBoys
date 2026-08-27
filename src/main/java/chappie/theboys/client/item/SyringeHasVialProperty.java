package chappie.theboys.client.item;


import chappie.theboys.common.item.datacomponents.TBDataComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record SyringeHasVialProperty() implements SelectItemModelProperty<Boolean> {
	public static final SelectItemModelProperty.Type<SyringeHasVialProperty, Boolean> TYPE = SelectItemModelProperty.Type.create(
		MapCodec.unit(new SyringeHasVialProperty()), Codec.BOOL
	);

	@Override
	public @Nullable Boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
		return entity == null ? null : !stack.getOrDefault(TBDataComponents.VIAL, ItemStack.EMPTY).isEmpty();
	}

	@Override
	public SelectItemModelProperty.Type<SyringeHasVialProperty, Boolean> type() {
		return TYPE;
	}

	@Override
	public Codec<Boolean> valueCodec() {
		return Codec.BOOL;
	}
}