package chappie.theboys.mixin;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.SuperHearingAbility;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Shadow
    @Final
    List<ServerPlayer> players;

    @Inject(method = "explode", at = @At("TAIL"))
    public void getDistance(Entity source, DamageSource damageSource, ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius, boolean fire, Level.ExplosionInteraction explosionInteraction, CallbackInfoReturnable<Explosion> cir, @Local Explosion explosion) {
        for (ServerPlayer player : this.players) {
            if (player.distanceToSqr(x, y, z) > 4096.0D) {
                for (SuperHearingAbility a : CommonUtil.listOfType(SuperHearingAbility.class, CommonUtil.getAbilities(player))) {
                    if (a.isEnabled()) {
                        player.connection.send(new ClientboundExplodePacket(x, y, z, radius, explosion.getToBlow(), (Vec3)explosion.getHitPlayers().get(player)));
                    }
                }
            }
        }
    }
}