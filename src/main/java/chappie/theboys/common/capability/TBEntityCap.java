package chappie.theboys.common.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class TBEntityCap implements INBTSerializable<CompoundTag> {

    public final Entity entity;
    private int glowingTick;

    public TBEntityCap(Entity entity) {
        this.entity = entity;
    }

    public static TBEntityCap getCap(Object provider) {
        if (provider instanceof Entity entity) {
            return entity.getData(TBAttachments.ENTITY_CAP);
        }
        return null;
    }

    public void setGlowingTick(int glowingTick) {
        this.glowingTick = glowingTick;
        if (this.entity != null) {
            this.entity.setData(TBAttachments.ENTITY_CAP, this);
        }
    }

    public boolean isGlowing() {
        return this.glowingTick > 0;
    }

    public void tick() {
        if (this.glowingTick > 0) {
            this.glowingTick--;
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("glowingTick", this.glowingTick);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.glowingTick = tag.getInt("glowingTick");
    }
}
