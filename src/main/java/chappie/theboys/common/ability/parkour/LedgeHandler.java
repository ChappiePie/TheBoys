package chappie.theboys.common.ability.parkour;

import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.KeyMap;
import chappie.modulus.util.data.DataAccessor;
import chappie.modulus.util.data.DataManager;
import chappie.theboys.common.ability.ParkourAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LedgeHandler extends ParkourHandler {

    public static final DataAccessor<Boolean> IS_HANGING = new DataAccessor<>("is_hanging", DataAccessor.DataSerializer.BOOLEAN);
    public static final DataAccessor<Boolean> IS_MANTLING = new DataAccessor<>("is_mantling", DataAccessor.DataSerializer.BOOLEAN);
    public static final DataAccessor<Float> INITIAL_BODY_ROT = new DataAccessor<>("initial_body_rot", DataAccessor.DataSerializer.FLOAT);
    public static final DataAccessor<Float> WALL_YAW = new DataAccessor<>("wall_yaw", DataAccessor.DataSerializer.FLOAT);
    public static final DataAccessor<Integer> MANTLE_TICKS = new DataAccessor<>("mantle_ticks", DataAccessor.DataSerializer.INT);
    public static final DataAccessor<Vec3> MANTLE_TARGET = new DataAccessor<>("mantle_target", DataAccessor.DataSerializer.VEC_3);
    public static final DataAccessor<Vec3> LEDGE_FORWARD = new DataAccessor<>("ledge_forward", DataAccessor.DataSerializer.VEC_3);
    public static final DataAccessor<Integer> FACING_DIRECTION = new DataAccessor<>("facing_direction", DataAccessor.DataSerializer.INT);

    public final IHasTimer.Cooldown vaultCooldown = new IHasTimer.Cooldown();
    public final IHasTimer.Timer animTimer = new IHasTimer.Timer(() -> 4, this::isHanging);
    public final IHasTimer.Timer vaultTimer;
    public final IHasTimer.Timer leftTimer;
    public final IHasTimer.Timer rightTimer;

    public LedgeHandler(ParkourAbility parkourAbility) {
        super(parkourAbility);
        this.vaultTimer = new IHasTimer.Timer(() -> 7, () -> !this.isHanging() && !this.vaultCooldown.end() && this.parkourAbility.authoritativeVelocity.y > 0);
        this.leftTimer = new IHasTimer.Timer(() -> 6, () -> this.isHanging() && this.parkourAbility.dataManager.get(ParkourAbility.STRAFE_IMPULSE) > 0);
        this.rightTimer = new IHasTimer.Timer(() -> 6, () -> this.isHanging() && this.parkourAbility.dataManager.get(ParkourAbility.STRAFE_IMPULSE) < 0);
    }

    @Override
    public List<IHasTimer.Timer> timers() {
        return List.of(this.animTimer, this.vaultTimer, this.leftTimer, this.rightTimer, this.vaultCooldown);
    }

    @Override
    public void defineData(DataManager dataManager) {
        dataManager.define(IS_HANGING, false);
        dataManager.define(IS_MANTLING, false);
        dataManager.define(INITIAL_BODY_ROT, 0.0F);
        dataManager.define(WALL_YAW, 0.0F);
        dataManager.define(MANTLE_TICKS, 0);
        dataManager.define(MANTLE_TARGET, Vec3.ZERO);
        dataManager.define(LEDGE_FORWARD, Vec3.ZERO);
        dataManager.define(FACING_DIRECTION, 0);
    }

    @Override
    public void tick(Player player) {
        if (this.isMantling()) {
            this.tickMantle(player);
        } else if (this.isHanging()) {
            this.tickHang(player);
        } else if (player.isInWater() || player.getAbilities().flying || player.onGround()) {
            this.resetHang();
        }
    }

    @Override
    public boolean canActivate(Player player) {
        return this.parkourAbility.activationHandlers.stream()
                .filter(p -> p != this && p != this.parkourAbility.dodgeRollHandler)
                .noneMatch(ParkourHandler::isActive);
    }

    @Override
    public boolean tryActivate(Player player) {
        if (!this.vaultCooldown.end() || this.parkourAbility.currentVelocity(player).y > 0.0D) {
            return false;
        }

        return this.checkForLedge(player, player.level());
    }

    private void tickHang(Player player) {
        if (!this.isWallStillThere(player, player.level())) {
            this.resetHang();
            return;
        }

        this.updateFacing(player);
        this.lockRot(player);

        if (this.parkourAbility.consumeBufferedJump()) {
            Vec3 forward = this.parkourAbility.dataManager.get(LEDGE_FORWARD);
            BlockPos target = player.blockPosition().offset((int) Math.round(forward.x), 1, (int) Math.round(forward.z));

            this.startMantle(player, target, forward);
            this.parkourAbility.dataManager.set(IS_HANGING, false);
            this.vaultCooldown.start(8);

            player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.2F);
            return;
        }

        if (this.parkourAbility.keys.isDown(KeyMap.KeyType.CROUCH)) {
            this.resetHang();
            this.vaultCooldown.start(14);
            return;
        }

        Vec3 fwd = this.parkourAbility.dataManager.get(LEDGE_FORWARD);
        Vec3 move = Vec3.ZERO;

        if (fwd.lengthSqr() >= 0.01D) {
            double strafe = this.parkourAbility.dataManager.get(ParkourAbility.STRAFE_IMPULSE);
            move = new Vec3(fwd.z, 0.0D, -fwd.x).normalize().scale(strafe * 0.02D);
        }

        this.parkourAbility.setControlledMovement(player, new Vec3(move.x, 0.0D, move.z));
        if (player instanceof ServerPlayer s) {
            s.connection.send(new ClientboundSetEntityMotionPacket(s));
        }
        player.fallDistance = 0.0F;
    }

    private void updateFacing(Player player) {
        Vec3 fwd = this.parkourAbility.dataManager.get(LEDGE_FORWARD);
        if (fwd.lengthSqr() < 0.01D) {
            this.parkourAbility.dataManager.set(FACING_DIRECTION, 0);
            return;
        }

        Vec3 look = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
        double dot = fwd.normalize().dot(look);
        double cross = fwd.x * look.z - fwd.z * look.x;

        FacingDirection dir = dot > 0.342D ? FacingDirection.TO_WALL : (cross < 0.0D ? FacingDirection.RIGHT_SIDE : FacingDirection.LEFT_SIDE);
        this.parkourAbility.dataManager.set(FACING_DIRECTION, dir.ordinal());
    }

    private void startMantle(Player player, BlockPos pos, Vec3 fwd) {
        Vec3 mFwd = this.parkourAbility.safeNormalize(fwd, player.getLookAngle());
        Vec3 offset = player.position().subtract(Vec3.atBottomCenterOf(player.blockPosition()));
        Vec3 target = Vec3.atBottomCenterOf(pos).add(offset).add(mFwd.scale(0.08D));

        this.parkourAbility.dataManager.set(IS_MANTLING, true);
        this.parkourAbility.dataManager.set(MANTLE_TICKS, 0);
        this.parkourAbility.dataManager.set(MANTLE_TARGET, target);
        this.parkourAbility.dataManager.set(LEDGE_FORWARD, mFwd);

        this.parkourAbility.setControlledMovement(player, Vec3.ZERO);
    }

    private boolean canGrab(Player player, Level lvl) {
        Vec3 look = this.parkourAbility.horizontal(player.getLookAngle());
        if (look.lengthSqr() < 1.0E-5D) {
            return false;
        }

        Vec3 fwd = look.normalize();
        Vec3 eye = player.getEyePosition();

        BlockHitResult cHit = this.parkourAbility.performRaycast(eye.subtract(0.0D, 0.5D, 0.0D), fwd.scale(0.7D), lvl, player);
        BlockHitResult hHit = this.parkourAbility.performRaycast(eye, fwd.scale(0.7D), lvl, player);

        if (cHit.getType() != HitResult.Type.BLOCK && hHit.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        BlockPos wall = (cHit.getType() == HitResult.Type.BLOCK ? cHit : hHit).getBlockPos();
        return lvl.getBlockState(wall.above()).isAir() && lvl.getBlockState(wall.above(2)).isAir() && lvl.getBlockState(wall.above(3)).isAir();
    }

    private boolean isWallStillThere(Player player, Level lvl) {
        Vec3 fwd = this.parkourAbility.dataManager.get(LEDGE_FORWARD);
        if (fwd.lengthSqr() < 0.01D) {
            return false;
        }

        Vec3 dir = fwd.normalize().scale(0.7D);
        Vec3 eye = player.getEyePosition();

        return this.parkourAbility.performRaycast(eye.subtract(0.0D, 0.5D, 0.0D), dir, lvl, player).getType() == HitResult.Type.BLOCK ||
                this.parkourAbility.performRaycast(eye, dir, lvl, player).getType() == HitResult.Type.BLOCK;
    }

    private void tickMantle(Player player) {
        Vec3 target = this.parkourAbility.dataManager.get(MANTLE_TARGET);
        Vec3 to = target.subtract(player.position());
        int ticks = this.parkourAbility.dataManager.get(MANTLE_TICKS);

        if (to.lengthSqr() < 0.055D || ticks > 10) {
            this.resetHang();
            player.fallDistance = 0.0F;
            return;
        }

        if (ticks == 2) {
            Vec3 move = new Vec3(0.0D, to.y * 0.75D, 0.0D);
            this.parkourAbility.setControlledMovement(player, player.getDeltaMovement().add(move));
            if (player instanceof ServerPlayer s) {
                s.connection.send(new ClientboundSetEntityMotionPacket(s));
            }
        } else if (ticks == 8) {
            Vec3 hor = this.parkourAbility.horizontal(to).scale(0.14D);
            double y = Mth.clamp(to.y * 0.28D, -0.04D, 0.18D);
            this.parkourAbility.setControlledMovement(player, new Vec3(hor.x, y, hor.z));
            if (player instanceof ServerPlayer s) {
                s.connection.send(new ClientboundSetEntityMotionPacket(s));
            }
        }

        player.fallDistance = 0.0F;
        this.parkourAbility.dataManager.set(MANTLE_TICKS, ticks + 1);
    }

    private boolean checkForLedge(Player player, Level lvl) {
        if (!this.canGrab(player, lvl)) {
            return false;
        }

        this.parkourAbility.slideHandler.stopSlide(false);
        this.parkourAbility.dodgeRollHandler.stopRoll();
        this.parkourAbility.wallHandler.reset();

        Vec3 fwd = this.parkourAbility.horizontal(player.getLookAngle()).normalize();
        float yaw = Mth.wrapDegrees((float) (-Mth.atan2(fwd.x, fwd.z) * Mth.RAD_TO_DEG));

        this.parkourAbility.dataManager.set(IS_HANGING, true);
        this.parkourAbility.dataManager.set(INITIAL_BODY_ROT, player.yBodyRot);
        this.parkourAbility.dataManager.set(LEDGE_FORWARD, fwd);
        this.parkourAbility.dataManager.set(WALL_YAW, yaw);

        this.snap(player);
        this.lockRot(player);

        player.playSound(SoundEvents.WOOL_PLACE, 0.8F, 1.0F);
        return true;
    }

    private void lockRot(Player player) {
        Vec3 fwd = this.parkourAbility.dataManager.get(LEDGE_FORWARD);
        if (fwd.lengthSqr() < 0.01D) {
            return;
        }

        float offset = this.ease(this.leftTimer.value(0.0F)) * 12.0F - this.ease(this.rightTimer.value(0.0F)) * 12.0F;
        float initial = this.parkourAbility.dataManager.get(INITIAL_BODY_ROT);
        float wallYaw = this.parkourAbility.dataManager.get(WALL_YAW);

        float next = Mth.rotLerp(0.22F, initial, wallYaw + offset);
        this.parkourAbility.dataManager.set(INITIAL_BODY_ROT, next);

        player.yBodyRotO = player.yBodyRot;
        player.yBodyRot = next;
    }

    public void applyTurn(Player player, double y, double x) {
        if (this.parkourAbility.dataManager.get(LEDGE_FORWARD).lengthSqr() < 0.01D) {
            return;
        }

        float yaw = this.parkourAbility.dataManager.get(WALL_YAW);
        float desired = (float) (player.getYRot() + y * 0.15D);
        float head = Mth.wrapDegrees(yaw + Mth.clamp(Mth.wrapDegrees(desired - yaw), -45.0F, 45.0F));
        float pitch = Mth.clamp((float) (player.getXRot() + x * 0.15D), -85.0F, 85.0F);

        player.yRotO = player.getYRot();
        player.xRotO = player.getXRot();
        player.setYRot(head);
        player.setXRot(pitch);
        player.yHeadRotO = player.yHeadRot = head;

        this.lockRot(player);
    }

    private void snap(Player player) {
        Vec3 nudge = this.parkourAbility.dataManager.get(LEDGE_FORWARD).normalize().scale(-0.04D);
        AABB box = player.getBoundingBox().move(nudge);

        if (player.level().noCollision(player, box)) {
            player.setPos(player.getX() + nudge.x, player.getY(), player.getZ() + nudge.z);
        }
    }

    private float ease(float v) {
        v = Mth.clamp(v, 0.0F, 1.0F);
        return v * v * (3.0F - 2.0F * v);
    }

    public boolean isHanging() {
        return this.parkourAbility.dataManager.get(IS_HANGING);
    }

    public boolean isMantling() {
        return this.parkourAbility.dataManager.get(IS_MANTLING);
    }

    @Override
    public void reset() {
        this.resetHang();
        this.vaultCooldown.timer = 0;
    }

    public void resetHang() {
        this.parkourAbility.dataManager.set(IS_HANGING, false);
        this.parkourAbility.dataManager.set(IS_MANTLING, false);
        this.parkourAbility.dataManager.set(MANTLE_TICKS, 0);
        this.parkourAbility.dataManager.set(MANTLE_TARGET, Vec3.ZERO);
        this.parkourAbility.dataManager.set(LEDGE_FORWARD, Vec3.ZERO);
        this.parkourAbility.dataManager.set(WALL_YAW, 0.0F);
        this.parkourAbility.dataManager.set(INITIAL_BODY_ROT, 0.0F);
        this.parkourAbility.dataManager.set(FACING_DIRECTION, 0);
    }

    @Override
    public boolean isActive() {
        return this.isHanging() || this.isMantling();
    }

    public enum FacingDirection {
        TO_WALL, LEFT_SIDE, RIGHT_SIDE
    }
}
