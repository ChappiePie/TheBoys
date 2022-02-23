package chappie.theboys.mixin;

import chappie.theboys.abilities.TBAbilityTypes;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;

import java.util.List;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Shadow @Final List<ServerPlayer> players;

    @Inject(method = "explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Explosion$BlockInteraction;)Lnet/minecraft/world/level/Explosion;", at = @At("TAIL"))
    public void getDistance(Entity pExploder, DamageSource pDamageSource, ExplosionDamageCalculator pContext, double pX, double pY, double pZ, float pSize, boolean pCausesFire, Explosion.BlockInteraction pMode, CallbackInfoReturnable<Explosion> cir) {
        for (ServerPlayer player : this.players) {
            if (player.distanceToSqr(pX, pY, pZ) > 4096.0D) {
                for (Ability ability : AbilityHelper.getAbilities(player)) {
                    if (ability.type == TBAbilityTypes.SUPER_HEARING && ability.getEnabled()) {
                        player.connection.send(new ClientboundExplodePacket(pX, pY, pZ, pSize, cir.getReturnValue().getToBlow(), cir.getReturnValue().getHitPlayers().get(player)));
                    }
                }
            }
        }
    }
}
