package chappie.theboys.mixin.client;

import chappie.theboys.client.ClientEvents;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {

    @ModifyVariable(method = "play", at = @At("HEAD"), argsOnly = true)
    public SoundInstance theBoys$changeSound(SoundInstance soundInstance) {
        return ClientEvents.playSound(soundInstance);
    }
}
