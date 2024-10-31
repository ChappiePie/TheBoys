package chappie.theboys.util.tooltip;

import chappie.theboys.common.ability.base.TBSuperpower;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record SuperpowerTooltip(TBSuperpower superpower) implements TooltipComponent {
}