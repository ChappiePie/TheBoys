package chappie.theboys.mixin.client;

import chappie.theboys.util.ISimpleSoundInstance;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractSoundInstance.class)
public class AbstractSoundInstanceMixin implements ISimpleSoundInstance {

    @Shadow protected float volume;

    @Override
    public void setVolume(float volume) {
        this.volume = volume;
    }

    @Override
    public float volume() {
        return this.volume;
    }
}
