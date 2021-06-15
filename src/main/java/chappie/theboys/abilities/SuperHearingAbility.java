package chappie.theboys.abilities;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.LogicalSide;
import xyz.heroesunited.heroesunited.common.abilities.JSONAbility;

public class SuperHearingAbility extends JSONAbility {

    public SuperHearingAbility() {
        super(TBAbilityTypes.SUPER_HEARING);
    }

    @Override
    public void onUpdate(PlayerEntity player, LogicalSide side) {
        if (side == LogicalSide.CLIENT && Minecraft.getInstance() != null) {
            Minecraft mc = Minecraft.getInstance();
            float volume = mc.options.getSoundSourceVolume(SoundCategory.MASTER);
            mc.getSoundManager().updateSourceVolume(SoundCategory.MASTER, getEnabled() ? volume * 99999 : volume);
        }
    }
}
