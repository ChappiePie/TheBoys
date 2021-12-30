package chappie.theboys.common.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static chappie.theboys.common.capability.BoysCap.CAPABILITY;

public class BoysProvider implements ICapabilitySerializable<CompoundTag> {

    public LazyOptional<IBoys> instance;

    public BoysProvider(Player player) {
        instance = LazyOptional.of(() -> new BoysCap(player));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == CAPABILITY ? instance.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.orElseThrow(() -> new IllegalArgumentException("HUPlayer must not be empty")).serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.orElseThrow(() -> new IllegalArgumentException("HUPlayer must not be empty!")).deserializeNBT(nbt);
    }
}
