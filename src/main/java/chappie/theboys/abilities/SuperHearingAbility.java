package chappie.theboys.abilities;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import xyz.heroesunited.heroesunited.common.abilities.JSONAbility;

public class SuperHearingAbility extends JSONAbility {

    public SuperHearingAbility() {
        super(TBAbilityTypes.SUPER_HEARING);
    }

    @Override
    public void onUpdate(Player player) {
        if (Minecraft.getInstance() != null) {
            Minecraft mc = Minecraft.getInstance();
            float volume = mc.options.getSoundSourceVolume(SoundSource.MASTER);
            mc.getSoundManager().updateSourceVolume(SoundSource.MASTER, getEnabled() ? volume * 99999 : volume);
        }
    }
}
