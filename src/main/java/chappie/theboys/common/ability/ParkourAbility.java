package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.AbilityClientProperties;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.KeyMap;
import chappie.modulus.util.data.DataAccessor;
import chappie.modulus.util.events.SetupAnimCallback;
import chappie.theboys.common.ability.parkour.*;
import chappie.theboys.util.interfaces.ILivingEntityEx;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class ParkourAbility extends Ability implements IHasTimer {

    public static final DataAccessor<Integer> STRAFE_IMPULSE = new DataAccessor<>("strafe_impulse", DataAccessor.DataSerializer.INT);
    public static final DataAccessor<Integer> FORWARD_IMPULSE = new DataAccessor<>("forward_impulse", DataAccessor.DataSerializer.INT);

    public final SlideHandler slideHandler = new SlideHandler(this);
    public final DodgeRollHandler dodgeRollHandler = new DodgeRollHandler(this);
    public final WallHandler wallHandler = new WallHandler(this);
    public final LedgeHandler ledgeHandler = new LedgeHandler(this);

    public final ImmutableList<ParkourHandler> activationHandlers;
    private final ImmutableList<ParkourHandler> handlers;

    public Vec3 authoritativeVelocity = Vec3.ZERO;
    private boolean jumpWasDown;
    private int jumpBuffer;

    public ParkourAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
        this.handlers = ImmutableList.of(slideHandler, dodgeRollHandler, wallHandler, ledgeHandler);
        this.activationHandlers = ImmutableList.of(wallHandler, ledgeHandler, dodgeRollHandler, slideHandler);

        for (ParkourHandler handler : this.handlers) {
            handler.defineData(this.dataManager);
        }
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(STRAFE_IMPULSE, 0, false);
        this.dataManager.define(FORWARD_IMPULSE, 0, false);
    }

    @Override
    public Iterable<Timer> timers() {
        return () -> this.handlers.stream()
                .flatMap(h -> h.timers().stream())
                .iterator();
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);

        if (!enabled || !(entity instanceof Player player)) {
            this.handlers.forEach(ParkourHandler::reset);
            this.jumpWasDown = false;
            this.jumpBuffer = 0;
            return;
        }

        if (player.isSwimming() || player.isFallFlying() || player.getAbilities().flying) {
            this.slideHandler.reset();
            this.dodgeRollHandler.reset();
            this.wallHandler.reset();
            this.ledgeHandler.reset();
            this.authoritativeVelocity = Vec3.ZERO;
            this.jumpWasDown = false;
            this.jumpBuffer = 0;
            return;
        }

        if (entity.level().isClientSide()) {
            int x = Math.round(entity.xxa);
            int z = Math.round(entity.zza);
            if (this.dataManager.get(STRAFE_IMPULSE) != x) {
                this.dataManager.setFromClient(STRAFE_IMPULSE, x);
            }
            if (this.dataManager.get(FORWARD_IMPULSE) != z) {
                this.dataManager.setFromClient(FORWARD_IMPULSE, z);
            }
        }

        this.authoritativeVelocity = this.calculateAuthoritativeVelocity(player);

        boolean jumpDown = this.keys.isDown(KeyMap.KeyType.JUMP);
        if (jumpDown && !this.jumpWasDown) {
            this.jumpBuffer = 5;
        } else if (this.jumpBuffer > 0) {
            this.jumpBuffer--;
        }
        this.jumpWasDown = jumpDown;

        for (ParkourHandler handler : this.activationHandlers) {
            handler.tick(player);
        }

        boolean active = this.activationHandlers.stream().anyMatch(ParkourHandler::isActive) || this.tryActivateParkour(player);
        if (!active) {
            this.tickJumpMovement(player);
        }

        if (!player.level().isClientSide()) {
            double cap = this.slideHandler.isActive() ? 0.82D : (player.onGround() ? 0.72D : 0.58D);
            Vec3 horizontal = this.horizontal(this.currentVelocity(player));

            if (horizontal.lengthSqr() > cap * cap) {
                Vec3 clamped = horizontal.normalize().scale(cap);
                this.setControlledMovement(player, new Vec3(clamped.x, this.vanillaVertical(player), clamped.z));
            }
        }
    }

    private void tickJumpMovement(Player player) {
        if (player.onGround() || this.wallHandler.isActive()) {
            return;
        }

        Vec3 input = this.movementInput(player);
        if (input.lengthSqr() <= 1.0E-5D) {
            return;
        }

        Vec3 velocity = this.horizontal(this.currentVelocity(player)).add(input.scale(0.012D));
        Vec3 horizontal = this.clampHorizontal(velocity, 0.58D);

        player.setDeltaMovement(horizontal.x, this.vanillaVertical(player), horizontal.z);
        player.fallDistance = Math.min(player.fallDistance, 1.0F);
    }

    public boolean consumeBufferedJump() {
        if (this.jumpBuffer <= 0) {
            return false;
        }
        this.jumpBuffer = 0;
        return true;
    }

    private boolean tryActivateParkour(Player player) {
        for (ParkourHandler handler : this.activationHandlers) {
            if (handler.canActivate(player)) {
                if (handler.tryActivate(player)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void initializeClient(Consumer<AbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new AbilityClientProperties() {
            @Override
            public void setupAnim(SetupAnimCallback.SetupAnimEvent event) {
                HumanoidModel<?> model = event.model();
                float pt = event.modelProperties().partialTicks();

                if (wallHandler.wallKickCooldown.value(pt) > 0) {
                    animateWallKick(model, pt);
                }
                if (wallHandler.getSlideProgress(pt) > 0) {
                    animateWallSlide(model, pt);
                }
                if (wallHandler.isWallRunning()) {
                    animateWallRun(model, pt);
                }
                if (slideHandler.isActive() || slideHandler.slideExitTicks > 0) {
                    animateSlide(model, pt, event.entity().tickCount);
                }
                if (dodgeRollHandler.isActive() || (dodgeRollHandler instanceof DodgeRollHandler dr && dr.rollTimer.value(pt) > 0)) {
                    animateDodgeRoll(model, pt);
                }
                if (ledgeHandler.isActive() || ledgeHandler.vaultTimer.value(pt) > 0) {
                    animateLedgeHang(model, pt);
                }
            }


            private void animateSlide(HumanoidModel<?> model, float pt, int ticks) {
                float progress = slideHandler.slideProgress(pt);
                if (progress <= 0) {
                    return;
                }

                float smooth = progress * progress * (3.0F - 2.0F * progress);
                float age = ticks + pt;
                float damp = progress / 4.0F + 1.0F;

                float bob = Mth.sin(age * 0.15F) * 0.08F * smooth;
                float breathe = Mth.sin(age * 0.08F) * 0.05F * smooth;
                float sway = Mth.cos(age * 0.12F) * 0.06F * smooth;

                model.head.xRot = model.head.xRot / damp + toRad(-20) * smooth;
                model.head.yRot = model.head.yRot / damp + toRad(-32) * smooth;
                model.head.zRot = Mth.lerp(smooth, model.head.zRot, toRad(8));
                model.body.xRot = Mth.lerp(smooth, model.body.xRot, toRad(-62));
                model.body.yRot = Mth.lerp(smooth, model.body.yRot, toRad(-20));
                model.body.zRot = Mth.lerp(smooth, model.body.zRot, toRad(24));

                model.rightArm.xRot = Mth.lerp(smooth, model.rightArm.xRot, toRad(-15) + bob);
                model.rightArm.yRot = Mth.lerp(smooth, model.rightArm.yRot, toRad(48) + sway * 0.8F);
                model.rightArm.zRot = Mth.lerp(smooth, model.rightArm.zRot, toRad(38) + bob * 0.7F);
                model.leftArm.xRot = Mth.lerp(smooth, model.leftArm.xRot, toRad(48) - bob * 0.8F);
                model.leftArm.yRot = Mth.lerp(smooth, model.leftArm.yRot, toRad(52) - sway * 0.6F);
                model.leftArm.zRot = Mth.lerp(smooth, model.leftArm.zRot, toRad(-18) - bob * 0.6F);

                model.rightLeg.xRot = Mth.lerp(smooth, model.rightLeg.xRot, toRad(-54) + breathe * 0.5F);
                model.rightLeg.yRot = Mth.lerp(smooth, model.rightLeg.yRot, toRad(4) + sway * 0.4F);
                model.rightLeg.zRot = Mth.lerp(smooth, model.rightLeg.zRot, toRad(14) + sway * 0.5F);
                model.leftLeg.xRot = Mth.lerp(smooth, model.leftLeg.xRot, toRad(-68) - breathe * 0.6F);
                model.leftLeg.yRot = Mth.lerp(smooth, model.leftLeg.yRot, toRad(-24) + sway * 0.5F);
                model.leftLeg.zRot = Mth.lerp(smooth, model.leftLeg.zRot, toRad(30) + sway * 0.6F);

                model.head.y -= 1.5F * smooth;
                model.head.z -= 1.2F * smooth;
                model.body.y -= 2.2F * smooth;
                model.body.z += 0.5F * smooth;
                model.rightArm.y -= (2.0F + bob * 0.7F) * smooth;
                model.rightArm.z -= (2.0F + bob * 0.6F) * smooth;
                model.leftArm.y -= (2.0F - bob * 0.7F) * smooth;
                model.leftArm.z -= (1.0F - bob * 0.5F) * smooth;
                model.rightLeg.x += (0.3F + sway * 0.4F) * smooth;
                model.rightLeg.y -= (8.5F + breathe * 0.7F) * smooth;
                model.rightLeg.z -= (10.5F + bob * 0.6F) * smooth;
                model.leftLeg.x += (1.5F + sway * 0.5F) * smooth;
                model.leftLeg.y -= (7.0F - breathe * 0.6F) * smooth;
                model.leftLeg.z -= (8.5F - bob * 0.5F) * smooth;
            }

            private void animateDodgeRoll(HumanoidModel<?> model, float pt) {
                float progress = dodgeRollHandler.rollTimer.value(pt);
                if (progress <= 0) {
                    return;
                }

                float smooth = progress * progress * (3.0F - 2.0F * progress);
                float prep = Mth.clamp(progress / 0.3F, 0.0F, 1.0F);
                float tuck = Mth.clamp((progress - 0.3F) / 0.7F, 0.0F, 1.0F);
                float damp = 1.0F + progress * 2.5F;

                model.head.xRot = Mth.lerp(smooth, model.head.xRot / damp, toRad(45));
                model.head.yRot /= damp;
                model.head.zRot /= damp;
                model.body.xRot = Mth.lerp(smooth, model.body.xRot / damp, toRad(25));
                model.body.yRot /= damp;
                model.body.zRot /= damp;

                float armX = Mth.lerp(tuck, toRad(-140), toRad(-85));
                model.rightArm.xRot = Mth.lerp(prep, model.rightArm.xRot / damp, armX);
                model.rightArm.yRot = Mth.lerp(smooth, model.rightArm.yRot / damp, toRad(-15) * tuck);
                model.rightArm.zRot = Mth.lerp(smooth, model.rightArm.zRot / damp, toRad(12) * tuck);
                model.leftArm.xRot = Mth.lerp(prep, model.leftArm.xRot / damp, armX);
                model.leftArm.yRot = Mth.lerp(smooth, model.leftArm.yRot / damp, toRad(15) * tuck);
                model.leftArm.zRot = Mth.lerp(smooth, model.leftArm.zRot / damp, toRad(-12) * tuck);

                model.rightLeg.xRot = Mth.lerp(smooth, model.rightLeg.xRot / damp, toRad(-90));
                model.rightLeg.yRot = Mth.lerp(smooth, model.rightLeg.yRot / damp, toRad(8));
                model.rightLeg.zRot = Mth.lerp(smooth, model.rightLeg.zRot / damp, toRad(6));
                model.leftLeg.xRot = Mth.lerp(smooth, model.leftLeg.xRot / damp, toRad(-90));
                model.leftLeg.yRot = Mth.lerp(smooth, model.leftLeg.yRot / damp, toRad(-8));
                model.leftLeg.zRot = Mth.lerp(smooth, model.leftLeg.zRot / damp, toRad(-6));

                model.rightArm.z += 1.5F * tuck;
                model.leftArm.z += 1.5F * tuck;
                model.rightLeg.z += 3.0F * smooth;
                model.leftLeg.z += 3.0F * smooth;
            }

            private void animateWallKick(HumanoidModel<?> model, float pt) {
                float progress = wallHandler.wallKickCooldown.value(pt);
                if (progress <= 0) {
                    return;
                }

                float smooth = progress * progress * (3.0F - 2.0F * progress);
                float damp = 1.0F + progress * 3.0F;

                model.body.xRot = model.body.xRot / damp + toRad(-15) * smooth;
                model.rightArm.xRot = model.rightArm.xRot / damp + toRad(-80) * smooth;
                model.rightArm.zRot = model.rightArm.zRot / damp + toRad(30) * smooth;
                model.leftArm.xRot = model.leftArm.xRot / damp + toRad(-80) * smooth;
                model.leftArm.zRot = model.leftArm.zRot / damp + toRad(-30) * smooth;
                model.rightLeg.xRot = model.rightLeg.xRot / damp + toRad(-60) * smooth;
                model.leftLeg.xRot = model.leftLeg.xRot / damp + toRad(-60) * smooth;
            }

            private void animateWallRun(HumanoidModel<?> model, float pt) {
                var dir = wallHandler.getWallDirection();
                if (dir == null) {
                    return;
                }

                float yaw = switch (dir) {
                    case SOUTH -> 0F;
                    case WEST -> 90F;
                    case NORTH -> 180F;
                    case EAST -> 270F;
                    default -> 0F;
                };

                float rel = Mth.wrapDegrees(entity.yBodyRot - yaw);
                model.body.xRot = toRad(-10);

                float cycle = Mth.sin(entity.tickCount * 0.6662F) * 0.8F;
                if (Math.abs(rel) < 45.0F) {
                    model.rightArm.xRot = toRad(-70) + cycle * 0.5F;
                    model.leftArm.xRot = toRad(-70) - cycle * 0.5F;
                } else if (rel <= -45.0F && rel >= -135.0F) {
                    model.rightArm.xRot = toRad(-90);
                    model.rightArm.zRot = toRad(-20);
                    model.leftArm.xRot = toRad(-40) + cycle;
                } else if (rel >= 45.0F && rel <= 135.0F) {
                    model.leftArm.xRot = toRad(-90);
                    model.leftArm.zRot = toRad(20);
                    model.rightArm.xRot = toRad(-40) - cycle;
                }

                model.rightLeg.xRot = cycle * 1.2F;
                model.leftLeg.xRot = -cycle * 1.2F;
            }

            private void animateWallSlide(HumanoidModel<?> model, float pt) {
                float slide = wallHandler.getSlideProgress(pt);
                var dir = wallHandler.getWallDirection();
                if (slide <= 0 || dir == null) {
                    return;
                }

                float yaw = switch (dir) {
                    case SOUTH -> 0F;
                    case WEST -> 90F;
                    case NORTH -> 180F;
                    case EAST -> 270F;
                    default -> 0F;
                };

                float rel = Mth.wrapDegrees(entity.yBodyRot - yaw);
                model.body.xRot = Mth.lerp(slide, model.body.xRot, toRad(-8));

                if (rel >= 45.0F && rel <= 135.0F) {
                    model.rightArm.xRot = Mth.lerp(slide, model.rightArm.xRot, toRad(-145));
                    model.rightArm.yRot = Mth.lerp(slide, model.rightArm.yRot, toRad(35));
                    model.rightArm.zRot = Mth.lerp(slide, model.rightArm.zRot, toRad(-25));
                    model.leftArm.xRot = Mth.lerp(slide, model.leftArm.xRot, toRad(-30));
                    model.leftArm.yRot = Mth.lerp(slide, model.leftArm.yRot, toRad(5));
                    model.leftArm.zRot = Mth.lerp(slide, model.leftArm.zRot, toRad(-10));
                } else if (rel <= -45.0F && rel >= -135.0F) {
                    model.leftArm.xRot = Mth.lerp(slide, model.leftArm.xRot, toRad(-145));
                    model.leftArm.yRot = Mth.lerp(slide, model.leftArm.yRot, toRad(-35));
                    model.leftArm.zRot = Mth.lerp(slide, model.leftArm.zRot, toRad(25));
                    model.rightArm.xRot = Mth.lerp(slide, model.rightArm.xRot, toRad(-30));
                    model.rightArm.yRot = Mth.lerp(slide, model.rightArm.yRot, toRad(-5));
                    model.rightArm.zRot = Mth.lerp(slide, model.rightArm.zRot, toRad(10));
                } else {
                    model.rightArm.xRot = Mth.lerp(slide, model.rightArm.xRot, toRad(-90));
                    model.rightArm.yRot = Mth.lerp(slide, model.rightArm.yRot, toRad(-8));
                    model.rightArm.zRot = Mth.lerp(slide, model.rightArm.zRot, toRad(12));
                    model.leftArm.xRot = Mth.lerp(slide, model.leftArm.xRot, toRad(-90));
                    model.leftArm.yRot = Mth.lerp(slide, model.leftArm.yRot, toRad(8));
                    model.leftArm.zRot = Mth.lerp(slide, model.leftArm.zRot, toRad(-12));
                }

                model.rightLeg.xRot = Mth.lerp(slide, model.rightLeg.xRot, toRad(-15));
                model.leftLeg.xRot = Mth.lerp(slide, model.leftLeg.xRot, toRad(15));
            }

            private void animateLedgeHang(HumanoidModel<?> model, float pt) {
                float hang = ledgeHandler.animTimer.value(pt);
                float vault = ledgeHandler.vaultTimer.value(pt);

                if (hang > 0) {
                    float damp = 1.0F + hang * 3.0F;
                    model.rightArm.xRot = model.rightArm.xRot / damp - 2.6F * hang;
                    model.leftArm.xRot = model.leftArm.xRot / damp - 2.6F * hang;
                    model.body.xRot = model.body.xRot / damp + 0.3F * hang;
                    model.rightLeg.z += 3.0F * hang;
                    model.leftLeg.z += 3.0F * hang;
                }

                if (vault > 0) {
                    float damp = 1.0F + vault * 3.0F;
                    model.body.xRot = model.body.xRot / damp + 0.8F * vault;
                    model.rightLeg.xRot = model.rightLeg.xRot / damp - 1.4F * vault;
                    model.leftLeg.xRot = model.leftLeg.xRot / damp - 1.4F * vault;
                }
            }

            private float toRad(float d) {
                return d * (float)(Math.PI / 180.0);
            }
        });
    }

    public BlockHitResult performRaycast(Vec3 start, Vec3 dir, Level level, Player player) {
        return level.clip(new ClipContext(start, start.add(dir), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
    }

    public Vec3 movementInput(Player player) {
        Vec3 forward = this.horizontal(player.getLookAngle());
        Vec3 input = forward.scale(this.dataManager.get(FORWARD_IMPULSE))
                .add(new Vec3(forward.z, 0, -forward.x).scale(this.dataManager.get(STRAFE_IMPULSE)));

        return input.lengthSqr() > 1.0D ? input.normalize() : input;
    }

    public Vec3 horizontal(Vec3 vec) {
        return new Vec3(vec.x, 0, vec.z);
    }

    public Vec3 safeNormalize(Vec3 vec, Vec3 fallback) {
        Vec3 horizontal = this.horizontal(vec);
        if (horizontal.lengthSqr() > 1.0E-5D) {
            return horizontal.normalize();
        }

        Vec3 fb = this.horizontal(fallback);
        return fb.lengthSqr() > 1.0E-5D ? fb.normalize() : Vec3.ZERO;
    }

    public Vec3 clampHorizontal(Vec3 vec, double max) {
        return vec.lengthSqr() > max * max ? vec.normalize().scale(max) : vec;
    }

    public Vec3 calculateAuthoritativeVelocity(Player player) {
        if (player.level().isClientSide()) {
            return player.getDeltaMovement();
        }

        if (player instanceof ILivingEntityEx ex) {
            Vec3 delta = player.position().subtract(ex.theBoys$oldPos());
            if (Double.isFinite(delta.x) && Double.isFinite(delta.y) && Double.isFinite(delta.z)) {
                return delta;
            }
        }
        return Vec3.ZERO;
    }

    public Vec3 currentVelocity(Player player) {
        return player.level().isClientSide() ? player.getDeltaMovement() : this.authoritativeVelocity;
    }

    public void setControlledMovement(Player player, Vec3 velocity) {
        Vec3 clamped = new Vec3(velocity.x, Mth.clamp(velocity.y, -1.2, 0.72), velocity.z);
        this.authoritativeVelocity = clamped;
        player.setDeltaMovement(clamped);

        if (!player.level().isClientSide()) {
            player.hurtMarked = true;
        }
        player.hasImpulse = true;
    }

    public double vanillaVertical(Player player) {
        return Mth.clamp(player.getDeltaMovement().y, -1.2, 0.62);
    }
}
