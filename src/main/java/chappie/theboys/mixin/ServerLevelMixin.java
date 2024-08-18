package chappie.theboys.mixin;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.SuperHearingAbility;
import com.llamalad7.mixinextras.sugar.Local;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Shadow @Final List<ServerPlayer> players;

    @Inject(method = "explode", at = @At("TAIL"))
    public void getDistance(Entity pSource, DamageSource pDamageSource, ExplosionDamageCalculator pDamageCalculator, double pX, double pY, double pZ, float pRadius, boolean pFire, Level.ExplosionInteraction pExplosionInteraction, ParticleOptions pSmallExplosionParticles, ParticleOptions pLargeExplosionParticles, SoundEvent pExplosionSound, CallbackInfoReturnable<Explosion> cir, @Local Explosion explosion) {
        for (ServerPlayer player : this.players) {
            if (player.distanceToSqr(pX, pY, pZ) > 4096.0D) {
                for (SuperHearingAbility a : CommonUtil.listOfType(SuperHearingAbility.class, CommonUtil.getAbilities(player))) {
                    if (a.isEnabled()) {
                        player.connection.send(new ClientboundExplodePacket(pX, pY, pZ, pRadius, cir.getReturnValue().getToBlow(), cir.getReturnValue().getHitPlayers().get(player), explosion.getBlockInteraction(), pSmallExplosionParticles, pLargeExplosionParticles, pExplosionSound));
                    }
                }
            }
        }
    }
}