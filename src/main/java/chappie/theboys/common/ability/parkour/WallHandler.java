package chappie.theboys.common.ability.parkour;

import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.KeyMap;
import chappie.modulus.util.data.DataAccessor;
import chappie.modulus.util.data.DataManager;
import chappie.theboys.common.ability.ParkourAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class WallHandler extends ParkourHandler {

    public static final DataAccessor<Boolean> IS_WALL_SLIDING = new DataAccessor<>("is_wall_sliding", DataAccessor.DataSerializer.BOOLEAN);
    public static final DataAccessor<Boolean> IS_WALL_RUNNING = new DataAccessor<>("is_wall_running", DataAccessor.DataSerializer.BOOLEAN);
    public static final DataAccessor<Double> WALL_RUN_HEIGHT = new DataAccessor<>("wall_run_height", DataAccessor.DataSerializer.DOUBLE);
    public static final DataAccessor<Integer> STRAFE_BLOCK_TICKS = new DataAccessor<>("strafe_block_ticks", DataAccessor.DataSerializer.INT);

    public final IHasTimer.Cooldown wallKickCooldown = new IHasTimer.Cooldown();
    public final IHasTimer.Timer wallGrabTimer = new IHasTimer.Timer(() -> 5, this::isWallGrabbing);

    private Direction wallDir;
    private Vec3 wallNorm = Vec3.ZERO;
    private boolean offsetApplied;
    private double runStartY;

    public WallHandler(ParkourAbility parkourAbility) {
        super(parkourAbility);
    }

    @Override
    public List<IHasTimer.Timer> timers() {
        return List.of(this.wallGrabTimer, this.wallKickCooldown);
    }

    @Override
    public void defineData(DataManager dataManager) {
        dataManager.define(IS_WALL_SLIDING, false);
        dataManager.define(IS_WALL_RUNNING, false);
        dataManager.define(WALL_RUN_HEIGHT, 0.0D);
        dataManager.define(STRAFE_BLOCK_TICKS, 0);
    }

    @Override
    public void reset() {
        this.parkourAbility.dataManager.set(IS_WALL_SLIDING, false);
        this.parkourAbility.dataManager.set(IS_WALL_RUNNING, false);
        this.parkourAbility.dataManager.set(WALL_RUN_HEIGHT, 0.0D);
        this.parkourAbility.dataManager.set(STRAFE_BLOCK_TICKS, 0);

        this.wallDir = null;
        this.wallNorm = Vec3.ZERO;
        this.offsetApplied = false;
        this.runStartY = 0.0D;
    }

    @Override
    public void tick(Player player) {
        int strafeTicks = this.parkourAbility.dataManager.get(STRAFE_BLOCK_TICKS);
        if (strafeTicks > 0) {
            strafeTicks--;
            this.parkourAbility.dataManager.set(STRAFE_BLOCK_TICKS, strafeTicks);
        }

        boolean running = this.parkourAbility.dataManager.get(IS_WALL_RUNNING);
        boolean sliding = this.parkourAbility.dataManager.get(IS_WALL_SLIDING);

        if (!running && !sliding) {
            if (this.wallGrabTimer.value(1.0F) == 0.0F) {
                this.wallDir = null;
                this.wallNorm = Vec3.ZERO;
            }
            return;
        }

        if (running) {
            if (!this.canContinueRun(player)) {
                this.parkourAbility.dataManager.set(IS_WALL_RUNNING, false);

                if (this.parkourAbility.ledgeHandler.canActivate(player) && this.parkourAbility.ledgeHandler.tryActivate(player)) {
                    return;
                }

                if (this.canSlide(player)) {
                    this.parkourAbility.dataManager.set(IS_WALL_SLIDING, true);
                }
                return;
            }
            this.performRun(player);
        } else {
            BlockPos belowPos = BlockPos.containing(player.position().subtract(0.0D, 1.5D, 0.0D));
            if (!this.canSlide(player) || !player.level().getBlockState(belowPos).isAir()) {
                this.parkourAbility.dataManager.set(IS_WALL_SLIDING, false);
                return;
            }
            this.performSlide(player);
        }

        if (this.wallKickCooldown.end() && this.parkourAbility.consumeBufferedJump()) {
            this.performKick(player);
        }
    }

    private boolean canSlide(Player player) {
        if (player.onGround() || this.parkourAbility.currentVelocity(player).y >= -0.1D) {
            return false;
        }

        Detection res = this.detect(player);
        if (!res.hit) {
            return false;
        }

        this.wallDir = res.dir;
        this.wallNorm = res.norm;

        Vec3 look = this.parkourAbility.horizontal(player.getLookAngle()).normalize();
        Vec3 toWall = this.wallNorm.scale(-1.0D).normalize();
        double dot = look.dot(toWall);

        return Math.toDegrees(Math.acos(Math.max(-1.0D, Math.min(1.0D, dot)))) <= 130.0D;
    }

    private boolean canStartRun(Player player) {
        if (!this.wallKickCooldown.end()) {
            return false;
        }

        Detection res = this.detect(player);
        if (!res.hit) {
            return false;
        }

        this.wallDir = res.dir;
        this.wallNorm = res.norm;

        Vec3 hVel = this.parkourAbility.horizontal(this.parkourAbility.currentVelocity(player));
        if (hVel.length() < 0.15D) {
            return false;
        }

        Vec3 velDir = hVel.normalize();
        Vec3 toWall = this.wallNorm.scale(-1.0D).normalize();
        double dot = velDir.dot(toWall);

        return Math.toDegrees(Math.acos(Math.max(-1.0D, Math.min(1.0D, dot)))) <= 80.0D;
    }

    private boolean canContinueRun(Player player) {
        if (player.getY() - this.runStartY >= 2.0D) {
            return false;
        }

        Detection res = this.detect(player);
        if (!res.hit) {
            return false;
        }

        this.wallDir = res.dir;
        this.wallNorm = res.norm;

        Vec3 look = this.parkourAbility.horizontal(player.getLookAngle()).normalize();
        Vec3 toWall = this.wallNorm.scale(-1.0D).normalize();
        double dot = look.dot(toWall);

        return Math.toDegrees(Math.acos(Math.max(-1.0D, Math.min(1.0D, dot)))) <= 130.0D;
    }

    private void performRun(Player player) {
        double height = player.getY() - this.runStartY;
        this.parkourAbility.dataManager.set(WALL_RUN_HEIGHT, height);
        this.applyOffset(player);

        Vec3 hVel = this.parkourAbility.horizontal(player.getDeltaMovement());
        double vSpeed = Math.max(0.35D - height * 0.1D, 0.0D);

        this.parkourAbility.setControlledMovement(player, new Vec3(hVel.x * 0.95D, vSpeed, hVel.z * 0.95D));
    }

    private void performSlide(Player player) {
        this.parkourAbility.dataManager.set(IS_WALL_SLIDING, true);
        this.applyOffset(player);

        Vec3 vel = player.getDeltaMovement();
        double y = vel.y < -0.25D ? Math.max(vel.y * 0.4D, -0.25D) : vel.y;

        this.parkourAbility.setControlledMovement(player, new Vec3(vel.x, y, vel.z));
    }

    private void applyOffset(Player player) {
        if (!this.offsetApplied && !this.wallNorm.equals(Vec3.ZERO)) {
            Vec3 push = this.wallNorm.normalize().scale(0.02D);
            player.setPos(player.getX() + push.x, player.getY(), player.getZ() + push.z);
            this.offsetApplied = true;
        }
    }

    private void performKick(Player player) {
        if (this.wallNorm.equals(Vec3.ZERO)) {
            return;
        }

        Vec3 currentVelocity = this.parkourAbility.currentVelocity(player);
        Vec3 hVel = this.parkourAbility.horizontal(currentVelocity);
        Vec3 norm = this.wallNorm.normalize();

        double dot = hVel.dot(norm);
        Vec3 refl = dot < 0.0D ? hVel.subtract(norm.scale(2.0D * dot)) : hVel.add(norm.scale(0.325D));

        Vec3 boost = refl.lengthSqr() > 0.001D
                ? refl.normalize().scale(Math.max(refl.length() * 1.3D, 0.65D))
                : norm.scale(0.65D);

        double vBoost = Math.max(currentVelocity.y, 0.0D) + 0.38D;

        this.parkourAbility.setControlledMovement(player, new Vec3(boost.x, vBoost, boost.z));

        this.wallKickCooldown.start(14);
        this.parkourAbility.dataManager.set(STRAFE_BLOCK_TICKS, 6);
        this.parkourAbility.dataManager.set(IS_WALL_SLIDING, false);
        this.parkourAbility.dataManager.set(IS_WALL_RUNNING, false);
        this.parkourAbility.dataManager.set(WALL_RUN_HEIGHT, 0.0D);
        this.runStartY = 0.0D;

        player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.5F, 1.2F);
    }

    private Detection detect(Player player) {
        Vec3 start = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        for (Direction d : Direction.Plane.HORIZONTAL) {
            Vec3 end = start.add(Vec3.atLowerCornerOf(d.getUnitVec3i()).scale(0.7D));
            BlockHitResult hit = player.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

            if (hit.getType() == HitResult.Type.BLOCK) {
                Direction face = hit.getDirection();
                return new Detection(true, face, Vec3.atLowerCornerOf(face.getUnitVec3i()));
            }
        }
        return new Detection(false, null, Vec3.ZERO);
    }

    public boolean isWallGrabbing() {
        return this.isActive();
    }

    public boolean isWallRunning() {
        return this.parkourAbility.dataManager.get(IS_WALL_RUNNING);
    }

    public float getSlideProgress(float pt) {
        return this.wallGrabTimer.value(pt);
    }

    public Direction getWallDirection() {
        return this.wallDir;
    }

    @Override
    public boolean canActivate(Player player) {
        return this.parkourAbility.activationHandlers.stream()
                .filter(p -> p != this && p != this.parkourAbility.dodgeRollHandler)
                .noneMatch(ParkourHandler::isActive);
    }

    @Override
    public boolean tryActivate(Player player) {
        if (this.canStartRun(player) && this.parkourAbility.consumeBufferedJump()) {
            this.parkourAbility.slideHandler.stopSlide(false);
            this.parkourAbility.dodgeRollHandler.stopRoll();
            this.parkourAbility.ledgeHandler.resetHang();

            this.parkourAbility.dataManager.set(IS_WALL_RUNNING, true);
            this.parkourAbility.dataManager.set(WALL_RUN_HEIGHT, 0.0D);
            this.runStartY = player.getY();
            this.offsetApplied = false;
            return true;
        }

        if (!this.canSlide(player)) {
            return false;
        }

        if (this.wallKickCooldown.end() && this.parkourAbility.consumeBufferedJump()) {
            this.performKick(player);
            return true;
        }

        if (!this.parkourAbility.keys.isDown(KeyMap.KeyType.MOUSE_RIGHT)) {
            return false;
        }

        this.parkourAbility.slideHandler.stopSlide(false);
        this.parkourAbility.dodgeRollHandler.stopRoll();
        this.parkourAbility.ledgeHandler.resetHang();

        this.performSlide(player);
        return true;
    }

    @Override
    public boolean isActive() {
        return this.parkourAbility.dataManager.get(IS_WALL_SLIDING) || this.parkourAbility.dataManager.get(IS_WALL_RUNNING);
    }

    private record Detection(boolean hit, Direction dir, Vec3 norm) {
    }
}