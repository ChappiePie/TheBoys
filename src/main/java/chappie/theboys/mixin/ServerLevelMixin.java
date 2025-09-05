package chappie.theboys.mixin;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.SuperHearingAbility;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
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
    public void getDistance(@Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius, boolean fire, Level.ExplosionInteraction explosionInteraction, ParticleOptions smallExplosionParticles, ParticleOptions largeExplosionParticles, Holder<SoundEvent> explosionSound, CallbackInfoReturnable<Explosion> cir, @Local Explosion explosion) {
        for (ServerPlayer player : this.players) {
            if (player.distanceToSqr(x, y, z) > 4096.0D) {
                for (SuperHearingAbility a : CommonUtil.listOfType(SuperHearingAbility.class, CommonUtil.getAbilities(player))) {
                    if (a.isEnabled()) {
                        player.connection
                                .send(
                                        new ClientboundExplodePacket(
                                                x,
                                                y,
                                                z,
                                                radius,
                                                explosion.getToBlow(),
                                                explosion.getHitPlayers().get(player),
                                                explosion.getBlockInteraction(),
                                                explosion.getSmallExplosionParticles(),
                                                explosion.getLargeExplosionParticles(),
                                                explosion.getExplosionSound()
                                        )
                                );
                    }
                }
            }
        }
    }
}