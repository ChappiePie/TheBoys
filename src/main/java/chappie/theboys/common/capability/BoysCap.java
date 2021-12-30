package chappie.theboys.common.capability;

import chappie.theboys.network.TBNetworking;
import chappie.theboys.network.client.ClientSyncBoysCap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.network.NetworkDirection;

import javax.annotation.Nonnull;

public class BoysCap implements IBoys {

    public static Capability<IBoys> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private final Player player;
    private boolean compoundV;

    public BoysCap(Player player) {
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
        if (player instanceof ServerPlayer) {
            TBNetworking.INSTANCE.sendTo(new ClientSyncBoysCap(player.getId(), this.serializeNBT()), ((ServerPlayer) player).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
        }
        return this;
    }

    @Override
    public IBoys syncToAll() {
        this.sync();
        for (Player player : this.player.level.players()) {
            if (player instanceof ServerPlayer) {
                TBNetworking.INSTANCE.sendTo(new ClientSyncBoysCap(this.player.getId(), this.serializeNBT()), ((ServerPlayer) player).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
            }
        }
        return this;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("CompoundV", this.compoundV);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("CompoundV")) {
            this.compoundV = nbt.getBoolean("CompoundV");
        }
    }

    @Nonnull
    public static IBoys getCap(Player player) {
        return player.getCapability(CAPABILITY).orElse(null);
    }
}