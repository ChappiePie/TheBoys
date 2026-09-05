package chappie.theboys.common.ability.parkour;

import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.data.DataAccessor;
import chappie.modulus.util.data.DataManager;
import chappie.theboys.common.ability.ParkourAbility;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SlideHandler extends ParkourHandler {

    public static final DataAccessor<Boolean> IS_SLIDING = new DataAccessor<>("is_sliding", DataAccessor.DataSerializer.BOOLEAN);
    public static final DataAccessor<Boolean> IS_STOPPING = new DataAccessor<>("is_stopping", DataAccessor.DataSerializer.BOOLEAN);
    public static final DataAccessor<Vec3> SLIDE_DIRECTION = new DataAccessor<>("slide_direction", DataAccessor.DataSerializer.VEC_3);
    public static final DataAccessor<Double> SLIDE_SPEED = new DataAccessor<>("slide_speed", DataAccessor.DataSerializer.DOUBLE);
    public static final DataAccessor<Integer> SLIDE_TICKS = new DataAccessor<>("slide_ticks", DataAccessor.DataSerializer.INT);

    public final IHasTimer.Cooldown slideCooldown = new IHasTimer.Cooldown();
    public final IHasTimer.Timer slideStartTimer = new IHasTimer.Timer(() -> 7, this::isActive);
    public final IHasTimer.Timer slideTimer = new IHasTimer.Timer(() -> 18, () -> this.slideStartTimer.ended() && this.isActive() && !this.parkourAbility.dataManager.get(IS_STOPPING));
    public final IHasTimer.Timer slideEndTimer = new IHasTimer.Timer(() -> 8, () -> (this.slideTimer.ended() || this.parkourAbility.dataManager.get(IS_STOPPING)) && this.isActive());

    public int slideTicksO;
    public int slideExitTicks;
    private int stopTicks;
    private boolean wasActive;

    public SlideHandler(ParkourAbility parkourAbility) {
        super(parkourAbility);
    }

    @Override
    public List<IHasTimer.Timer> timers() {
        return List.of(this.slideStartTimer, this.slideTimer, this.slideEndTimer, this.slideCooldown);
    }

    @Override
    public void defineData(DataManager dataManager) {
        dataManager.define(IS_SLIDING, false);
        dataManager.define(IS_STOPPING, false);
        dataManager.define(SLIDE_DIRECTION, Vec3.ZERO);
        dataManager.define(SLIDE_SPEED, 0.0D);
        dataManager.define(SLIDE_TICKS, 0);
    }

    @Override
    public void tick(Player player) {
        int ticks = this.parkourAbility.dataManager.get(SLIDE_TICKS);
        this.slideTicksO = ticks;

        if (this.slideExitTicks > 0) {
            this.slideExitTicks--;
        }

        if (player.level().isClientSide()) {
            if (this.stopTicks > 0) {
                this.stopTicks--;
                this.parkourAbility.dataManager.set(IS_STOPPING, true);
            }
        }

        boolean active = this.isActive();
        if (!active && this.wasActive && this.slideExitTicks == 0) {
            if (this.parkourAbility.activationHandlers.stream().noneMatch(ParkourHandler::isActive)) {
                this.slideExitTicks = 8;
            }
        }
        this.wasActive = active;

        if (!active) {
            return;
        }

        if (this.parkourAbility.consumeBufferedJump()) {
            this.stopSlide(true);

            Vec3 direction = this.parkourAbility.dataManager.get(SLIDE_DIRECTION);
            Vec3 horizontal = this.parkourAbility.safeNormalize(direction, this.parkourAbility.horizontal(player.getLookAngle()))
                    .scale(Mth.clamp(this.parkourAbility.currentVelocity(player).horizontalDistance() + 0.08D, 0.34D, 0.68D));

            this.parkourAbility.setControlledMovement(player, new Vec3(horizontal.x, 0.52D, horizontal.z));
            player.fallDistance = 0;
            player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 0.8F, 1.18F);
            return;
        }

        if (!player.onGround() || player.horizontalCollision) {
            this.parkourAbility.dataManager.set(IS_STOPPING, true);
            ticks++;
            this.parkourAbility.dataManager.set(SLIDE_TICKS, ticks);
            this.applyExitVel(player);
            return;
        }

        if (this.parkourAbility.dataManager.get(ParkourAbility.FORWARD_IMPULSE) < 0) {
            this.parkourAbility.dataManager.set(IS_STOPPING, true);
        }

        if (!this.parkourAbility.dataManager.get(IS_STOPPING)) {
            this.applySlideVel(player);
            this.spawnParticles(player);
        } else {
            this.applyExitVel(player);
        }

        if (this.slideEndTimer.ended()) {
            this.stopSlide(true);
        } else if (!this.parkourAbility.dataManager.get(IS_STOPPING) && ticks > 10 && this.parkourAbility.dataManager.get(SLIDE_SPEED) < 0.05D) {
            this.parkourAbility.dataManager.set(IS_STOPPING, true);
        }
    }

    @Override
    public boolean tryActivate(Player player) {
        if (!player.onGround() || !this.slideCooldown.end() || player.zza < -0.1F || !this.parkourAbility.dodgeRollHandler.rollCooldown.end()) {
            return false;
        }

        boolean hasSpeed = this.parkourAbility.horizontal(this.parkourAbility.currentVelocity(player)).length() >= 0.10D;
        if (!player.isShiftKeyDown() || (!player.isSprinting() && !hasSpeed)) {
            return false;
        }

        this.startSlide(player);
        return true;
    }

    public float slideProgress(float pt) {
        if (this.isActive()) {
            int ticks = this.parkourAbility.dataManager.get(SLIDE_TICKS);
            return ticks <= 5 ? Mth.lerp(pt, this.slideTicksO, ticks) / 5.0F : 1.0F;
        }
        return this.slideExitTicks > 0 ? Math.max(0, (this.slideExitTicks - pt) / 8.0F) : 0;
    }

    private void startSlide(Player player) {
        this.parkourAbility.dodgeRollHandler.stopRoll();
        this.parkourAbility.wallHandler.reset();
        this.parkourAbility.ledgeHandler.resetHang();

        Vec3 vel = this.parkourAbility.horizontal(this.parkourAbility.currentVelocity(player));
        if (vel.lengthSqr() < 0.01D && player.isSprinting()) {
            vel = this.parkourAbility.horizontal(player.getLookAngle()).normalize().scale(0.32D);
        }

        Vec3 dir = this.parkourAbility.safeNormalize(vel, this.parkourAbility.horizontal(player.getLookAngle()));

        this.parkourAbility.dataManager.set(IS_SLIDING, true);
        this.parkourAbility.dataManager.set(IS_STOPPING, false);
        this.parkourAbility.dataManager.set(SLIDE_TICKS, 0);
        this.parkourAbility.dataManager.set(SLIDE_DIRECTION, dir);
        this.parkourAbility.dataManager.set(SLIDE_SPEED, vel.length() * 2.25D);

        this.slideExitTicks = 0;

        BlockState state = player.level().getBlockState(player.blockPosition().below());
        if (!state.isAir()) {
            player.playSound(state.getSoundType().getStepSound(), 1.0F, 0.55F);
        }
        player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 0.7F, 0.9F);
    }

    private void applySlideVel(Player player) {
        int ticks = this.parkourAbility.dataManager.get(SLIDE_TICKS);
        ticks++;
        this.parkourAbility.dataManager.set(SLIDE_TICKS, ticks);

        Vec3 dir = this.parkourAbility.dataManager.get(SLIDE_DIRECTION);
        Vec3 look = this.parkourAbility.horizontal(player.getLookAngle()).normalize();
        dir = this.parkourAbility.safeNormalize(dir.scale(0.85D).add(look.scale(0.15D)), dir);

        double friction = player.onGround() ? 0.955D : 0.975D;
        double speed = Math.max(this.parkourAbility.dataManager.get(SLIDE_SPEED) * friction - 0.0035D, 0.0D);

        this.parkourAbility.dataManager.set(SLIDE_DIRECTION, dir);
        this.parkourAbility.dataManager.set(SLIDE_SPEED, speed);

        Vec3 move = dir.scale(speed);
        this.parkourAbility.setControlledMovement(player, new Vec3(move.x, this.parkourAbility.vanillaVertical(player), move.z));
        player.fallDistance = Math.min(player.fallDistance, 1.0F);
    }

    private void applyExitVel(Player player) {
        Vec3 horizontal = this.parkourAbility.horizontal(this.parkourAbility.currentVelocity(player));
        if (horizontal.lengthSqr() <= 1.0E-5D) {
            return;
        }

        double damping = player.onGround() ? 0.82D : 0.92D;
        Vec3 smooth = horizontal.lerp(horizontal.scale(damping), 0.45D);
        this.parkourAbility.setControlledMovement(player, new Vec3(smooth.x, this.parkourAbility.vanillaVertical(player), smooth.z));
    }

    private void spawnParticles(Player player) {
        if (!player.level().isClientSide() || player.tickCount % 2 != 0) {
            return;
        }

        Vec3 dir = this.parkourAbility.dataManager.get(SLIDE_DIRECTION);
        for (int i = 0; i < 2; i++) {
            player.level().addParticle(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    player.getX() + (Math.random() - 0.5D) * 0.45D,
                    player.getY() + 0.05D,
                    player.getZ() + (Math.random() - 0.5D) * 0.45D,
                    -dir.x * 0.025D, 0.02D, -dir.z * 0.025D
            );
        }
    }

    public void stopSlide(boolean cooldown) {
        this.parkourAbility.dataManager.set(IS_SLIDING, false);
        this.parkourAbility.dataManager.set(IS_STOPPING, false);
        this.parkourAbility.dataManager.set(SLIDE_TICKS, 0);
        this.parkourAbility.dataManager.set(SLIDE_DIRECTION, Vec3.ZERO);
        this.parkourAbility.dataManager.set(SLIDE_SPEED, 0.0D);

        this.slideExitTicks = cooldown ? 8 : 0;
        if (cooldown) {
            this.slideCooldown.start(28);
            if (this.parkourAbility.entity.level().isClientSide()) {
                this.stopTicks = 8;
            }
        }
    }

    @Override
    public void reset() {
        this.stopSlide(false);
        this.slideExitTicks = 0;
        this.slideTicksO = 0;
        this.slideCooldown.timer = 0;
        this.wasActive = false;
        this.stopTicks = 0;
    }

    @Override
    public boolean isActive() {
        return this.parkourAbility.dataManager.get(IS_SLIDING);
    }
}
