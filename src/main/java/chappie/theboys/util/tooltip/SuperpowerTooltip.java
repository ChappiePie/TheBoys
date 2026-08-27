package chappie.theboys.util.tooltip;

import chappie.modulus.common.ability.base.Superpower;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record SuperpowerTooltip(Superpower superpower) implements TooltipComponent {
}
