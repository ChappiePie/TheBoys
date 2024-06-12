package chappie.theboys.util;

import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.data.DataAccessor;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.UUID;

public class TBCommonUtil {
    public static final DataAccessor<Color> COLOR = new DataAccessor<>("color", DataAccessor.DataSerializer.COLOR);

    public static void spawnParticleForAll(Level world, ParticleOptions particleIn, boolean longDistanceIn, Vec3 posVc3d, Vec3 offsetVc3d, float speedIn, int countIn) {
        for (Player player : world.getEntitiesOfClass(Player.class, CommonUtil.boxWithRange(posVc3d, 20))) {
            if (player instanceof ServerPlayer) {
                ((ServerPlayer) player).connection.send(new ClientboundLevelParticlesPacket(particleIn, longDistanceIn, posVc3d.x, posVc3d.y, posVc3d.z, (float) offsetVc3d.x, (float) offsetVc3d.y, (float) offsetVc3d.z, speedIn, countIn));
            }
        }
    }

    public static void setAttribute(LivingEntity entity, String name, Attribute attribute, UUID uuid, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null || entity.level.isClientSide) {
            return;
        }

        AttributeModifier modifier = instance.getModifier(uuid);
        if (modifier != null && (amount == 0 || (modifier.getAmount() != amount || modifier.getOperation() != operation))) {
            instance.removeModifier(uuid);
            return;
        }

        if (modifier == null && amount != 0) {
            instance.addTransientModifier(new AttributeModifier(uuid, name, amount, operation));
        }
    }
}
