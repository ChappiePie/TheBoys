package chappie.theboys.common.capability;

import chappie.theboys.TheBoys;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

public class TBEntityCap implements AutoSyncedComponent, CommonTickingComponent, ComponentV3 {

    public static final ComponentKey<TBEntityCap> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(TheBoys.id("entity"), TBEntityCap.class);

    public final Entity entity;
    private int glowingTick;

    public TBEntityCap(Entity entity) {
        this.entity = entity;
    }

    public static TBEntityCap getCap(Object provider) {
        return KEY.maybeGet(provider).orElse(null);
    }

    public void setGlowingTick(int glowingTick) {
        this.glowingTick = glowingTick;
        KEY.sync(this.entity);
    }

    public boolean isGlowing() {
        return this.glowingTick > 0;
    }

    @Override
    public void tick() {
        if (this.glowingTick > 0) {
            this.glowingTick--;
        }
    }

    @Override
    public void readData(ValueInput input) {
        this.glowingTick = input.getIntOr("glowingTick", 0);
    }

    @Override
    public void writeData(ValueOutput output) {
        output.putInt("glowingTick", this.glowingTick);
    }
}