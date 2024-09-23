package chappie.theboys.common.capability;

import chappie.theboys.TheBoys;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class TBEntityCap implements AutoSyncedComponent, CommonTickingComponent, ComponentV3 {

    public static final ComponentKey<TBEntityCap> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(TheBoys.id("entity"), TBEntityCap.class);

    public final Entity entity;
    private int glowingTick;

    public TBEntityCap(Entity entity) {
        this.entity = entity;
    }

    public static TBEntityCap getCap(Object provider) {
        return KEY.maybeGet(provider).orElse(null);
    }

    public void setGlowingTick(int glowingTick) {
        this.glowingTick = glowingTick;
        KEY.sync(this.entity);
    }

    public boolean isGlowing() {
        return this.glowingTick > 0;
    }

    @Override
    public void tick() {
        if (this.glowingTick > 0) {
            this.glowingTick--;
        }
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        this.glowingTick = tag.getInt("glowingTick");
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putInt("glowingTick", this.glowingTick);
    }
}
