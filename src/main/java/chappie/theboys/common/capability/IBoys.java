package chappie.theboys.common.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public interface IBoys extends INBTSerializable<CompoundNBT> {

    int getSpeedLevel();
    void setSpeedLevel(int speedLevel);

    boolean isInSpeed();
    void setInSpeed(boolean isInSpeed);
}
