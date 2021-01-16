package chappie.theboys.common.capability;

import chappie.theboys.network.client.ClientSetCompoundV;
import chappie.theboys.network.client.ClientSetSlowMotion;
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

    private boolean slowMotion, compoundV;

    @Override
    public boolean isSlowMotion() {
        return slowMotion;
    }

    @Override
    public void setSlowMotion(boolean slowMotion) {
        this.slowMotion = slowMotion;
        if (!player.world.isRemote)
            TBNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new ClientSetSlowMotion(player.getEntityId(), slowMotion));
    }

    @Override
    public boolean haveCompoundV() {
        return compoundV;
    }

    @Override
    public void setCompoundV(boolean compoundV) {
        this.compoundV = compoundV;
        if (!player.world.isRemote)
            TBNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new ClientSetCompoundV(player.getEntityId(), compoundV));
    }


    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("SlowMotion", this.slowMotion);
        nbt.putBoolean("CompoundV", this.compoundV);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("SlowMotion")) {
            this.slowMotion = nbt.getBoolean("SlowMotion");
        }
        if (nbt.contains("CompoundV")) {
            this.compoundV = nbt.getBoolean("CompoundV");
        }
    }

    //-------------------------------------------------------------------------------------

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
