package chappie.theboys.common.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public interface IBoys extends INBTSerializable<CompoundNBT> {
    boolean haveCompoundV();
    void setCompoundV(boolean compoundV);
}
