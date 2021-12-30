package chappie.theboys.common.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IBoys extends INBTSerializable<CompoundTag> {
    boolean haveCompoundV();
    void setCompoundV(boolean compoundV);

    IBoys sync();

    IBoys syncToAll();
}
