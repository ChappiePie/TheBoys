package chappie.theboys.common.ability.base;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.util.KeyMap;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Supplier;

public class DoubleJumpCondition extends Condition {
    private Supplier<Boolean> shouldStop = () -> false;
    private boolean isJumped, enabled;
    private int jumpTriggerTime;

    public DoubleJumpCondition(Ability ability) {
        super(ability, (c) -> c instanceof DoubleJumpCondition k && k.enabled);
    }

    @Override
    public void update() {
        super.update();
        if (this.jumpTriggerTime > 0) {
            --this.jumpTriggerTime;
        }
        this.isJumped = this.ability.keys.isDown(KeyMap.KeyType.JUMP);
        if (this.shouldStop.get()) {
            this.enabled = false;
        }
    }

    @Override
    public void keyEvent() {
        super.keyEvent();
        if (!isJumped && this.ability.keys.isDown(KeyMap.KeyType.JUMP)) {
            if (this.jumpTriggerTime == 0) {
                this.jumpTriggerTime = 8;
            } else {
                this.enabled = !this.enabled;
                this.jumpTriggerTime = 0;
            }
        }
    }

    public DoubleJumpCondition shouldStop(Supplier<Boolean> shouldStop) {
        this.shouldStop = shouldStop;
        return this;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        tag.putBoolean("enabled", this.enabled);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        this.enabled = nbt.getBoolean("enabled");
    }
}
