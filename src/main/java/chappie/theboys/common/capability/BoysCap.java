package chappie.theboys.common.capability;

import chappie.theboys.network.SetInSpeedMessage;
import chappie.theboys.network.SetSpeedLevelMessage;
import chappie.theboys.network.TBNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BoysCap implements IBoys {

    private int speedLevel;
    private boolean isInSpeed;

    @Override
    public int getSpeedLevel() {
        return speedLevel;
    }

    @Override
    public void setSpeedLevel(int speedLevel) {
        this.speedLevel = speedLevel;
    }

    @Override
    public boolean isInSpeed() {
        return isInSpeed;
    }

    @Override
    public void setInSpeed(boolean isInSpeed) {
        this.isInSpeed = isInSpeed;
    }


    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("SpeedLevel", this.speedLevel);
        nbt.putBoolean("isInSpeed", this.isInSpeed);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("SpeedLevel")) {
            this.speedLevel = nbt.getInt("SpeedLevel");
        }
        if (nbt.contains("isInSpeed")) {
            this.isInSpeed = nbt.getBoolean("isInSpeed");
        }
    }

    @CapabilityInject(IBoys.class)
    public static Capability<IBoys> CAPABILITY;
    public final PlayerEntity player;

    public BoysCap(PlayerEntity player) {
        this.player = player;
    }

    @Nonnull
    public static IBoys getCap(PlayerEntity player) {
        IBoys cap = player.getCapability(CAPABILITY).orElse(null);
        return cap != null ? cap : null;
    }

    public static void setSpeedLevel(PlayerEntity player, IBoys a, int speedLevel) {
        a.setSpeedLevel(speedLevel);
        if (!player.world.isRemote)
            TBNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new SetSpeedLevelMessage(player.getEntityId(), speedLevel));
    }

    public static void setInSpeed(PlayerEntity player, IBoys a, boolean inSpeed) {
        a.setInSpeed(inSpeed);
        if (!player.world.isRemote)
            TBNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new SetInSpeedMessage(player.getEntityId(), inSpeed));
    }

    public static class BoysStorage implements Capability.IStorage<IBoys> {

        @Nullable
        @Override
        public INBT writeNBT(Capability<IBoys> capability, IBoys instance, Direction side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<IBoys> capability, IBoys instance, Direction side, INBT nbt) {
            instance.deserializeNBT(nbt instanceof CompoundNBT ? (CompoundNBT) nbt : new CompoundNBT());
        }
    }
}
