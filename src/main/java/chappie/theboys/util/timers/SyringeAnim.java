package chappie.theboys.util.timers;

import chappie.modulus.util.IHasTimer;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.item.SyringeItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SyringeAnim implements IHasTimer {
    public final Timer timeline = new Timer(() -> 25, () -> false);

    public boolean triggerAnim;

    public void tick(LivingEntity entity) {
        ItemStack mainHandItem = entity.getMainHandItem();
        ItemStack offHandItem = entity.getOffhandItem();
        this.timeline.predicate = () -> this.triggerAnim
                && mainHandItem.getItem() instanceof SyringeItem
                && offHandItem.isEmpty();
        float timeline = this.timeline.value(1);

        if (this.triggerAnim && !(mainHandItem.getItem() instanceof SyringeItem && offHandItem.isEmpty()) || timeline == 1 || entity.getUseItemRemainingTicks() > 0 && entity.getUseItemRemainingTicks() <= 10) {
            this.triggerAnim = false;
            TheBoysCap.getCap(entity).syncToAll();
        }

        this.timers().forEach(Timer::update);
    }

    @Override
    public Iterable<Timer> timers() {
        return List.of(this.timeline);
    }
}