package chappie.theboys.util.timers;

import chappie.modulus.util.IHasTimer;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.item.SyringeItem;
import chappie.theboys.common.item.VialItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SyringeVialAnim implements IHasTimer {
    public final IHasTimer.Timer timeline = new IHasTimer.Timer(() -> 20, () -> false);
    public final IHasTimer.Timer rollVial = new IHasTimer.Timer(() -> 6, () -> false);
    public final IHasTimer.Timer insertVial = new IHasTimer.Timer(() -> 3, () -> false);

    public boolean triggerAnim;

    public void tick(LivingEntity entity) {
        ItemStack mainHandItem = entity.getMainHandItem();
        ItemStack offHandItem = entity.getOffhandItem();
        this.timeline.predicate = () -> this.triggerAnim
                && mainHandItem.getItem() instanceof SyringeItem
                && offHandItem.getItem() instanceof VialItem;
        float timeline = this.timeline.value(1);

        if (timeline == 1 && !entity.getCommandSenderWorld().isClientSide()) {
            mainHandItem.getOrCreateTag().put("vial", offHandItem.save(new CompoundTag()));
            offHandItem.shrink(1);
        }

        boolean a = Math.min(timeline, 0.5F) * 2F == 1;
        this.rollVial.predicate = () -> a && timeline < 0.7F;
        this.insertVial.predicate = () -> a && timeline > 0.8F;

        if (this.triggerAnim && !(mainHandItem.getItem() instanceof SyringeItem) || timeline == 1) {
            this.triggerAnim = false;
            TheBoysCap.getCap(entity).syncToAll();
        }

        this.timers().forEach(Timer::update);
    }

    public boolean hideOffHand(Player player, TheBoysCap cap, float partialTicks, InteractionHand hand) {
        float t = cap.vialAnim.timeline.value(partialTicks);
        if (!cap.vialAnim.triggerAnim && t > 0 && hand == InteractionHand.OFF_HAND) {
            if (player.getMainHandItem().getItem() instanceof SyringeItem && player.getMainHandItem().getTag() != null) {
                return player.getMainHandItem().getTag().contains("vial");
            }
        }
        return false;
    }

    @Override
    public Iterable<Timer> timers() {
        return List.of(this.timeline, this.rollVial, this.insertVial);
    }
}