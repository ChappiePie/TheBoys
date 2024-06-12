package chappie.theboys.util.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record ArmorTooltip(ItemStack itemStack) implements TooltipComponent {
}