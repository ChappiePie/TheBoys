package chappie.theboys.mixin;

import chappie.theboys.util.ISimpleSoundInstance;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractSoundInstance.class)
public class AbstractSoundInstanceMixin implements ISimpleSoundInstance {

    @Shadow protected double x;

    @Shadow protected double y;

    @Shadow protected double z;

    @Shadow protected float volume;

    @Override
    public void setPosition(Vec3 position) {
        this.x = position.x();
        this.y = position.y();
        this.z = position.z();
        this.volume = Float.MAX_VALUE;
    }
}
