package chappie.theboys.util;

import chappie.theboys.common.capability.BoysCap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Timer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.UUID;

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

    public static void setAttribute(LivingEntity entity, String name, Attribute attribute, UUID uuid, double amount, AttributeModifier.Operation operation) {
        ModifiableAttributeInstance instance = entity.getAttribute(attribute);

        if (instance == null || entity.world.isRemote) {
            return;
        }

        AttributeModifier modifier = instance.getModifier(uuid);

        if (amount == 0 || modifier != null && (modifier.getAmount() != amount || modifier.getOperation() != operation)) {
            instance.removeModifier(uuid);
        }

        modifier = instance.getModifier(uuid);

        if (modifier == null) {
            modifier = new AttributeModifier(uuid, name, amount, operation);
            instance.applyNonPersistentModifier(modifier);
        }
    }
}
