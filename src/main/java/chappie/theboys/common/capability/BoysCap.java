package chappie.theboys.common.capability;

import chappie.theboys.network.TBNetworking;
import chappie.theboys.network.client.ClientSyncBoysCap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkDirection;

import javax.annotation.Nonnull;

public class BoysCap implements IBoys {

    @CapabilityInject(IBoys.class)
    public static Capability<IBoys> CAPABILITY;
    private final PlayerEntity player;
    private boolean compoundV;

    public BoysCap(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public boolean haveCompoundV() {
        return compoundV;
    }

    @Override
    public void setCompoundV(boolean compoundV) {
        this.compoundV = compoundV;
        this.syncToAll();
    }

    @Override
    public IBoys sync() {
        player.refreshDimensions();
        if (player instanceof ServerPlayerEntity) {
            TBNetworking.INSTANCE.sendTo(new ClientSyncBoysCap(player.getId(), this.serializeNBT()), ((ServerPlayerEntity) player).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
        }
        return this;
    }

    @Override
    public IBoys syncToAll() {
        this.sync();
        for (PlayerEntity player : this.player.level.players()) {
            if (player instanceof ServerPlayerEntity) {
                TBNetworking.INSTANCE.sendTo(new ClientSyncBoysCap(this.player.getId(), this.serializeNBT()), ((ServerPlayerEntity) player).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
            }
        }
        return this;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("CompoundV", this.compoundV);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("CompoundV")) {
            this.compoundV = nbt.getBoolean("CompoundV");
        }
    }

    @Nonnull
    public static IBoys getCap(PlayerEntity player) {
        return player.getCapability(CAPABILITY).orElse(null);
    }
}