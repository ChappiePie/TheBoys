package chappie.theboys.util.timers;

import chappie.modulus.util.IHasTimer;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.item.SyringeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SyringeAnim implements IHasTimer {
    public final Timer timeline = new Timer(() -> 25, () -> false);
    private final TheBoysCap boysCap;
    private boolean triggerAnim;

    public SyringeAnim(TheBoysCap boysCap) {
        this.boysCap = boysCap;
    }

    public void triggerAnim(boolean triggerAnim) {
        this.triggerAnim = triggerAnim;
        this.boysCap.syncToAll();
    }

    public void tick(LivingEntity entity) {
        ItemStack mainHandItem = entity.getMainHandItem();
        ItemStack offHandItem = entity.getOffhandItem();
        this.timeline.predicate = () -> this.triggerAnim
                && mainHandItem.getItem() instanceof SyringeItem
                && offHandItem.isEmpty();
        float timeline = this.timeline.value(1);

        if (this.triggerAnim && !(mainHandItem.getItem() instanceof SyringeItem && offHandItem.isEmpty()) || timeline == 1 || entity.getUseItemRemainingTicks() > 0 && entity.getUseItemRemainingTicks() <= 10) {
            this.triggerAnim(false);
        }

        this.timers().forEach(Timer::update);
    }

    public void readFromNbt(CompoundTag tag) {
        this.triggerAnim = tag.getBoolean("triggerAnim");
    }

    public CompoundTag writeToNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("triggerAnim", this.triggerAnim);
        return tag;
    }

    @Override
    public Iterable<Timer> timers() {
        return List.of(this.timeline);
    }
}