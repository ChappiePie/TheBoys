package chappie.theboys.util.timers;

import chappie.modulus.util.IHasTimer;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.item.SyringeItem;
import chappie.theboys.common.item.VialItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SyringeVialAnim implements IHasTimer {
    public final IHasTimer.Timer timeline = new IHasTimer.Timer(() -> 20, () -> false);
    public final IHasTimer.Timer rollVial = new IHasTimer.Timer(() -> 6, () -> false);
    public final IHasTimer.Timer insertVial = new IHasTimer.Timer(() -> 3, () -> false);
    private final TheBoysCap boysCap;
    private boolean triggerAnim, reverse;

    public SyringeVialAnim(TheBoysCap boysCap) {
        this.boysCap = boysCap;
    }

    public void triggerAnim(boolean trigger, boolean reverse) {
        this.triggerAnim = trigger;
        this.reverse = reverse;
        this.boysCap.syncToAll();
    }

    public void tick(LivingEntity entity) {
        ItemStack mainHandItem = entity.getMainHandItem();
        ItemStack offHandItem = entity.getOffhandItem();
        this.timeline.predicate = () -> this.triggerAnim
                && mainHandItem.getItem() instanceof SyringeItem
                && (offHandItem.getItem() instanceof VialItem || (this.reverse && mainHandItem.getTag() != null && mainHandItem.getTag().contains("vial") && offHandItem.isEmpty()));
        float timeline = this.timeline.value(1);
        if (timeline == 1 && !entity.getCommandSenderWorld().isClientSide()) {
            if (this.reverse) {
                ItemStack itemStack = ItemStack.of(mainHandItem.getOrCreateTag().getCompound("vial"));
                entity.setItemInHand(InteractionHand.OFF_HAND, itemStack);
                mainHandItem.getOrCreateTag().remove("vial");
            } else {
                mainHandItem.getOrCreateTag().put("vial", offHandItem.copyWithCount(1).save(new CompoundTag()));
                offHandItem.shrink(1);
            }
        }

        boolean a = Math.min(timeline, 0.5F) * 2F == 1;
        this.rollVial.predicate = () -> a && timeline < 0.7F;
        this.insertVial.predicate = () -> a && timeline > 0.8F;

        if (this.triggerAnim && !(mainHandItem.getItem() instanceof SyringeItem) || timeline == 1) {
            this.triggerAnim = false;
            this.boysCap.syncToAll();
        }

        this.timers().forEach(Timer::update);
    }

    public void readFromNbt(CompoundTag tag) {
        this.triggerAnim = tag.getBoolean("triggerAnim");
        this.reverse = tag.getBoolean("reverse");
    }

    public CompoundTag writeToNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("triggerAnim", this.triggerAnim);
        tag.putBoolean("reverse", this.reverse);
        return tag;
    }

    @Override
    public Iterable<Timer> timers() {
        return List.of(this.timeline, this.rollVial, this.insertVial);
    }
}