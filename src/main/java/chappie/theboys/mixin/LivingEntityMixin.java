package chappie.theboys.mixin;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.FlightAbility;
import chappie.theboys.common.ability.SpeedAbility;
import chappie.theboys.util.interfaces.EntitySavingFields;
import chappie.theboys.util.interfaces.ILivingEntityEx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ILivingEntityEx {

    @Unique
    private Vec3 oldPos = Vec3.ZERO;

    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void theBoys$setupOldPos(Vec3 pos) {
        this.oldPos = pos;
    }

    @Override
    public Vec3 theBoys$oldPos() {
        return this.oldPos;
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    public void mixin$tick(CallbackInfo ci) {
        this.oldPos = new Vec3(this.position().toVector3f());
    }

    @Inject(method = "getEyeHeight", at = @At("TAIL"), cancellable = true)
    public void mixin$getEyeHeight(Pose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir) {
        LivingEntity player = (LivingEntity) (Object) this;
        if (player != null && player.isAlive() && !player.position().equals(Vec3.ZERO)) {
            for (FlightAbility ability : CommonUtil.listOfType(FlightAbility.class, CommonUtil.getAbilities(player))) {
                if (player.isSprinting() && ability.isEnabled()) {
                    cir.setReturnValue(0.51F);
                }
            }
        }
    }

    @Inject(method = "maxUpStep()F", at = @At("RETURN"), cancellable = true)
    public void mixin$maxUpStep(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        for (SpeedAbility a : CommonUtil.listOfType(SpeedAbility.class, CommonUtil.getAbilities(entity))) {
            if (a.isEnabled() && !entity.isSwimming() && !entity.isFallFlying()) {
                cir.setReturnValue(cir.getReturnValue() + 1);
                break;
            }
        }
    }

    @Inject(method = "causeFallDamage", at = @At("TAIL"))
    public void mixin$causeFallDamage(float fallDistance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getCommandSenderWorld() instanceof ServerLevel level) {
            for (FlightAbility a : CommonUtil.listOfType(FlightAbility.class, CommonUtil.getAbilities(entity))) {
                if (a.dataManager.get(FlightAbility.BREAK_BLOCKS) && fallDistance > 20) {
                    for (int x = 0; x < 5; x++) {
                        for (int y = 0; y < 5; y++) {
                            for (int z = 0; z < 5; z++) {
                                double xPos = entity.getX() - 2.5 + x + entity.getCommandSenderWorld().random.nextInt(5);
                                double yPos = entity.getY() - 2.5 + y + entity.getCommandSenderWorld().random.nextInt(5);
                                double zPos = entity.getZ() - 2.5 + z + entity.getCommandSenderWorld().random.nextInt(5);
                                BlockPos pos = new BlockPos((int) xPos, (int) yPos, (int) zPos);
                                Block block = entity.getCommandSenderWorld().getBlockState(pos).getBlock();

                                if (block == Blocks.STONE) {
                                    entity.getCommandSenderWorld().setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());
                                } else if (block == Blocks.STONE_BRICKS) {
                                    entity.getCommandSenderWorld().setBlockAndUpdate(pos, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
                                } else if (block == Blocks.COBBLESTONE) {
                                    entity.getCommandSenderWorld().setBlockAndUpdate(pos, Blocks.GRAVEL.defaultBlockState());
                                } else if (block == Blocks.GRASS_BLOCK) {
                                    entity.getCommandSenderWorld().setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
                                } else if (block == Blocks.DIRT) {
                                    entity.getCommandSenderWorld().setBlockAndUpdate(pos, Blocks.COARSE_DIRT.defaultBlockState());
                                } else if (block == Blocks.OAK_LOG) {
                                    entity.getCommandSenderWorld().setBlockAndUpdate(pos, Blocks.STRIPPED_OAK_LOG.defaultBlockState());
                                } else if (block == Blocks.BIRCH_LOG) {
                                    entity.getCommandSenderWorld().setBlockAndUpdate(pos, Blocks.STRIPPED_BIRCH_LOG.defaultBlockState());
                                } else if (block == Blocks.SPRUCE_LOG) {
                                    entity.getCommandSenderWorld().setBlockAndUpdate(pos, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
                                } else if (block == Blocks.JUNGLE_LOG) {
                                    entity.getCommandSenderWorld().setBlockAndUpdate(pos, Blocks.STRIPPED_JUNGLE_LOG.defaultBlockState());
                                } else if (block == Blocks.DARK_OAK_LOG) {
                                    entity.getCommandSenderWorld().setBlockAndUpdate(pos, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
                                } else if (block == Blocks.ACACIA_LOG) {
                                    entity.getCommandSenderWorld().setBlockAndUpdate(pos, Blocks.STRIPPED_ACACIA_LOG.defaultBlockState());
                                } else if (block == Blocks.GLASS) {
                                    entity.getCommandSenderWorld().destroyBlock(pos, false);
                                }

                                for (ServerPlayer serverPlayer : level.getEntitiesOfClass(ServerPlayer.class, CommonUtil.boxWithRange(entity.position(), 30))) {
                                    level.sendParticles(serverPlayer, ParticleTypes.EXPLOSION, false, entity.getX(), entity.getY() + 0.25F, entity.getZ(), 0, (fallDistance / entity.getCommandSenderWorld().getHeight()) * 10, 0.0D, 0.0D, 1F);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "isFallFlying", at = @At("TAIL"), cancellable = true)
    public void mixin$getFallFlying(CallbackInfoReturnable<Boolean> cir) {
        var map = ((EntitySavingFields) this).map();
        if (map.containsKey("isFallFlying")) {
            cir.setReturnValue((boolean) map.get("isFallFlying"));
        } else {
            boolean b = false;
        }
    }

    @Inject(method = "getSwimAmount", at = @At("TAIL"), cancellable = true)
    public void mixin$getSwimAmount(CallbackInfoReturnable<Float> cir) {
        var map = ((EntitySavingFields) this).map();
        if (map.containsKey("swimAmount")) {
            cir.setReturnValue((float) map.get("swimAmount"));
        }
    }

    @Inject(method = "isVisuallySwimming", at = @At("TAIL"), cancellable = true)
    public void mixin$isVisuallySwimming(CallbackInfoReturnable<Boolean> cir) {
        var map = ((EntitySavingFields) this).map();
        if (map.containsKey("isVisuallySwimming")) {
            cir.setReturnValue((boolean) map.get("isVisuallySwimming"));
        }
    }

    @Inject(method = "getFallFlyingTicks", at = @At("TAIL"), cancellable = true)
    public void mixin$getFallFlyingTicks(CallbackInfoReturnable<Integer> cir) {
        var map = ((EntitySavingFields) this).map();
        if (map.containsKey("fallFlyingTicks")) {
            cir.setReturnValue((int) map.get("fallFlyingTicks"));
        }
    }
}
