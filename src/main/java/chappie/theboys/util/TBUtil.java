package chappie.theboys.util;

import chappie.theboys.common.capability.BoysCap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Timer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class TBUtil {

    public static float CLIENT_DEFAULT_TICKS = 20.0F;
    public static long MILISECONDS_PER_TICK = 50;

    //Slow-motion
    @OnlyIn(Dist.CLIENT)
    public static void updateClientTickrate(float tickrate) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        ObfuscationReflectionHelper.setPrivateValue(Minecraft.class, mc, new Timer(tickrate, 0l), "field_71428_T");
        if (mc.world != null && areAllPlayersSlowMotion(mc.world)) {
            updateServerTickrate(tickrate);
        }
    }

    public static void updateServerTickrate(float tickrate) {
        MILISECONDS_PER_TICK = (long) (1000L / tickrate);
    }

    public static boolean areAllPlayersSlowMotion(World world) {
        for (PlayerEntity player : world.getPlayers()) {
            if (!player.isAlive() && !BoysCap.getCap(player).isSlowMotion()) {
                return false;
            }
        }
        return true;
    }
}
