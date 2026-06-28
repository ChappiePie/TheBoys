package chappie.theboys.common.capability;

import chappie.theboys.networking.TBNetworking;
import chappie.theboys.networking.packet.SyncTheBoysCapPacket;
import chappie.theboys.util.timers.SyringeAnim;
import chappie.theboys.util.timers.SyringeVialAnim;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

public class TheBoysCap implements INBTSerializable<CompoundTag> {

    public final SyringeVialAnim vialAnim = new SyringeVialAnim(this);
    public final SyringeAnim syringeAnim = new SyringeAnim(this);
    private final LivingEntity livingEntity;
    private boolean compoundV;
    private int eyesHeight = 5, eyesLength = 1;

    public TheBoysCap(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
    }

    @Nullable
    public static TheBoysCap getCap(Object provider) {
        if (provider instanceof LivingEntity entity) {
            return entity.getData(TBAttachments.THEBOYS_CAP);
        }
        return null;
    }

    public void tick() {
        if (this.livingEntity != null && this.livingEntity.isAlive()) {
            this.vialAnim.tick(this.livingEntity);
            this.syringeAnim.tick(this.livingEntity);
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

    public void syncToAll() {
        if (this.livingEntity != null && !this.livingEntity.level().isClientSide()) {
            this.livingEntity.setData(TBAttachments.THEBOYS_CAP, this);
            TBNetworking.sendToTrackingEntityAndSelf(new SyncTheBoysCapPacket(this.serializeNBT(this.livingEntity.level().registryAccess())), this.livingEntity);
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("CompoundV", this.compoundV);

        CompoundTag eyeOptions = new CompoundTag();
        eyeOptions.putInt("eyesHeight", this.eyesHeight);
        eyeOptions.putInt("eyesLength", this.eyesLength);
        tag.put("eyeOptions", eyeOptions);

        tag.put("syringeAnim", this.syringeAnim.writeToNbt());
        tag.put("vialAnim", this.vialAnim.writeToNbt());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.compoundV = tag.getBoolean("CompoundV");
        CompoundTag nbt = tag.getCompound("eyeOptions");
        this.eyesHeight = nbt.getInt("eyesHeight");
        this.eyesLength = nbt.getInt("eyesLength");

        this.syringeAnim.readFromNbt(tag.getCompound("syringeAnim"));
        this.vialAnim.readFromNbt(tag.getCompound("vialAnim"));
    }
}
