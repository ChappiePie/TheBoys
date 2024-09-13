package chappie.theboys.common.capability;

import chappie.theboys.TheBoys;
import chappie.theboys.util.timers.SyringeVialAnim;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.CopyableComponent;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class TheBoysCap implements AutoSyncedComponent, CopyableComponent<TheBoysCap>, CommonTickingComponent, ComponentV3 {

    public static final ComponentKey<TheBoysCap> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(TheBoys.id("cap"), TheBoysCap.class);

    public static TheBoysCap getCap(Object provider) {
        return KEY.maybeGet(provider).orElse(null);
    }

    private final LivingEntity livingEntity;
    private boolean compoundV;
    private int eyesHeight = 5, eyesLength = 1;
    public final SyringeVialAnim vialAnim = new SyringeVialAnim();

    public TheBoysCap(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
    }

    @Override
    public void tick() {
        if (this.livingEntity.isAlive() && this.livingEntity instanceof Player player) {
            this.vialAnim.tick(player);
//            if (this.livingEntity.level().isClientSide()) {
//                PlayerAnimCap cap = PlayerAnimCap.getCap(player);
//                if (cap != null) {
//                    if (player.isCrouching()) {
//                        cap.triggerAnim("theboys_arm_controller", "dab");
//                    }
//                }
//            }
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
            KEY.sync(player);
        }
    }

    public void syncToAll() {
        this.sync();
        for (LivingEntity livingEntity : this.livingEntity.getCommandSenderWorld().players()) {
            if (livingEntity instanceof ServerPlayer player) {
                KEY.sync(player);
            }
        }
    }

    @Override
    public void copyFrom(TheBoysCap other) {
        this.compoundV = other.compoundV;
        this.eyesHeight = other.eyesHeight;
        this.eyesLength = other.eyesLength;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        this.compoundV = tag.getBoolean("CompoundV");
        CompoundTag nbt = tag.getCompound("eyeOptions");
        this.eyesHeight =  nbt.getInt("eyesHeight");
        this.eyesLength = nbt.getInt("eyesLength");
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putBoolean("CompoundV", this.compoundV);

        CompoundTag eyeOptions = new CompoundTag();
        eyeOptions.putInt("eyesHeight", this.eyesHeight);
        eyeOptions.putInt("eyesLength", this.eyesLength);
        tag.put("eyeOptions", eyeOptions);
    }
}
