package chappie.theboys.common.capability;

import chappie.theboys.TheBoys;
import chappie.theboys.util.timers.SyringeAnim;
import chappie.theboys.util.timers.SyringeVialAnim;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

public class TheBoysCap implements AutoSyncedComponent, CommonTickingComponent, ComponentV3 {

    public static final ComponentKey<TheBoysCap> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(TheBoys.id("cap"), TheBoysCap.class);
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
        return KEY.maybeGet(provider).orElse(null);
    }

    @Override
    public void tick() {
        if (this.livingEntity.isAlive()) {
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
        KEY.sync(this.livingEntity);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        //return player != this.livingEntity;
        return true;
    }

    @Override
    public void readData(ValueInput input) {
        this.compoundV = input.getBooleanOr("CompoundV", false);
        ValueInput eyeOptions = input.childOrEmpty("eyeOptions");
        this.eyesHeight = eyeOptions.getIntOr("eyesHeight", this.eyesHeight);
        this.eyesLength = eyeOptions.getIntOr("eyesLength", this.eyesLength);
        input.read("syringeAnim", CompoundTag.CODEC).ifPresent(this.syringeAnim::readFromNbt);
        input.read("vialAnim", CompoundTag.CODEC).ifPresent(this.vialAnim::readFromNbt);
    }

    @Override
    public void writeData(ValueOutput output) {
        output.putBoolean("CompoundV", this.compoundV);
        ValueOutput eyeOptions = output.child("eyeOptions");
        eyeOptions.putInt("eyesHeight", this.eyesHeight);
        eyeOptions.putInt("eyesLength", this.eyesLength);
        output.store("syringeAnim", CompoundTag.CODEC, this.syringeAnim.writeToNbt());
        output.store("vialAnim", CompoundTag.CODEC, this.vialAnim.writeToNbt());
    }
}
