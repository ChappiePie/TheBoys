package chappie.theboys.common.capability;

import chappie.theboys.networking.TBNetworking;
import chappie.theboys.networking.client.ClientSyncTheBoysCap;
import chappie.theboys.util.timers.SyringeVialAnim;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class TheBoysCap implements INBTSerializable<CompoundTag> {

    public static Capability<TheBoysCap> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    private final LivingEntity livingEntity;
    private boolean compoundV;
    private int eyesHeight = 5, eyesLength = 1;
    public final SyringeVialAnim vialAnim = new SyringeVialAnim();

    public TheBoysCap(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
    }

    @Nullable
    public static TheBoysCap getCap(Entity entity) {
        return entity.getCapability(TheBoysCap.CAPABILITY).orElse(null);
    }

    public void tick() {
        if (this.livingEntity instanceof Player player) {
            this.vialAnim.tick(player);
        }
    }

    public boolean compoundV() {
        return compoundV;
    }

    public void setCompoundV(boolean compoundV) {
        this.compoundV = compoundV;
        this.syncToAll();
    }

    public int eyesHeight() {
        return eyesHeight;
    }

    public int eyesLength() {
        return eyesLength;
    }

    public void setEyeOptions(int eyesHeight, int eyesLength) {
        if (this.eyesHeight != eyesHeight || this.eyesLength != eyesLength) {
            this.eyesHeight = eyesHeight;
            this.eyesLength = eyesLength;
            this.syncToAll();
        }
    }

    public void sync() {
        if (this.livingEntity instanceof ServerPlayer player) {
            TBNetworking.INSTANCE.send(new ClientSyncTheBoysCap(this.livingEntity.getId(), this.serializeNBT()), player.connection.getConnection());
        }
    }

    public void syncToAll() {
        this.sync();
        for (LivingEntity livingEntity : this.livingEntity.getCommandSenderWorld().players()) {
            if (livingEntity instanceof ServerPlayer player) {
                TBNetworking.INSTANCE.send(new ClientSyncTheBoysCap(this.livingEntity.getId(), this.serializeNBT()), player.connection.getConnection());
            }
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("CompoundV", this.compoundV);

        CompoundTag eyeOptions = new CompoundTag();
        eyeOptions.putInt("eyesHeight", this.eyesHeight);
        eyeOptions.putInt("eyesLength", this.eyesLength);
        tag.put("eyeOptions", eyeOptions);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.compoundV = tag.getBoolean("CompoundV");
        CompoundTag nbt = tag.getCompound("eyeOptions");
        this.eyesHeight =  nbt.getInt("eyesHeight");
        this.eyesLength = nbt.getInt("eyesLength");
    }
}
