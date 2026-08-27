package chappie.theboys.common.ability.parkour;

import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.data.DataAccessor;
import chappie.modulus.util.data.DataManager;
import chappie.theboys.common.ability.ParkourAbility;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DodgeRollHandler extends ParkourHandler {

    public static final DataAccessor<Boolean> IS_ROLLING = new DataAccessor<>("is_rolling", DataAccessor.DataSerializer.BOOLEAN);
    public static final DataAccessor<Integer> ROLL_TICKS = new DataAccessor<>("roll_ticks", DataAccessor.DataSerializer.INT);
    public static final DataAccessor<Vec3> ROLL_DIRECTION = new DataAccessor<>("roll_direction", DataAccessor.DataSerializer.VEC_3);
    public static final DataAccessor<Integer> ROLL_SIDE = new DataAccessor<>("roll_side", DataAccessor.DataSerializer.INT);
    public static final DataAccessor<Double> ROLL_SPEED = new DataAccessor<>("roll_speed", DataAccessor.DataSerializer.DOUBLE);

    public final IHasTimer.Cooldown rollCooldown = new IHasTimer.Cooldown();
    public final IHasTimer.Timer rollTimer = new IHasTimer.Timer(() -> 12, this::isActive);
    public int rollTicksO;

    public DodgeRollHandler(ParkourAbility parkourAbility) {
        super(parkourAbility);
    }

    @Override
    public List<IHasTimer.Timer> timers() {
        return List.of(this.rollTimer, this.rollCooldown);
    }

    @Override
    public void defineData(DataManager dataManager) {
        dataManager.define(IS_ROLLING, false);
        dataManager.define(ROLL_TICKS, 0);
        dataManager.define(ROLL_DIRECTION, Vec3.ZERO);
        dataManager.define(ROLL_SIDE, 0);
        dataManager.define(ROLL_SPEED, 0.0D);
    }

    @Override
    public void tick(Player player) {
        int rollTicks = this.parkourAbility.dataManager.get(ROLL_TICKS);
        this.rollTicksO = rollTicks;

        if (!this.isActive()) {
            return;
        }

        Vec3 input = this.parkourAbility.movementInput(player);
        rollTicks++;
        this.parkourAbility.dataManager.set(ROLL_TICKS, rollTicks);

        double speed = this.parkourAbility.dataManager.get(ROLL_SPEED);
        speed *= 0.985D;
        this.parkourAbility.dataManager.set(ROLL_SPEED, speed);

        Vec3 rollDirection = this.parkourAbility.dataManager.get(ROLL_DIRECTION);
        Vec3 carriedInput = input.lengthSqr() > 1.0E-5D ? input.scale(0.04D) : Vec3.ZERO;

        Vec3 horizontal = rollDirection.scale(speed).add(carriedInput);

        this.parkourAbility.setControlledMovement(player, new Vec3(horizontal.x, this.parkourAbility.vanillaVertical(player), horizontal.z));
        player.fallDistance = Math.min(player.fallDistance, 1.0F);

        if (rollTicks >= 14 || (player.horizontalCollision && rollTicks > 4)) {
            this.stopRoll();
        }
    }

    @Override
    public boolean tryActivate(Player player) {
        if (player.level().isClientSide()) {
            return false;
        }

        Vec3 input = this.parkourAbility.movementInput(player);

        if (!parkourAbility.conditionManager.test("roll") || input.lengthSqr() <= 0.04D || player.onGround() || !this.rollCooldown.end()) {
            return false;
        }

        this.startRoll(player, input);
        return true;
    }

    public boolean canTrigger() {
        return parkourAbility.isEnabled() && !isActive() && rollCooldown.end();
    }

    public void startRoll(Player player, Vec3 input) {
        this.parkourAbility.slideHandler.stopSlide(false);
        this.parkourAbility.wallHandler.reset();
        this.parkourAbility.ledgeHandler.resetHang();

        Vec3 currentVelocity = this.parkourAbility.horizontal(this.parkourAbility.currentVelocity(player));
        double speed = Math.max(0.48D, currentVelocity.length() * 1.15D);

        Vec3 fallback = currentVelocity.lengthSqr() > 1.0E-5D ? currentVelocity : this.parkourAbility.horizontal(player.getLookAngle());
        Vec3 rollDirection = this.parkourAbility.safeNormalize(input, fallback);

        Vec3 look = this.parkourAbility.horizontal(player.getLookAngle()).normalize();
        Vec3 roll = this.parkourAbility.horizontal(rollDirection).normalize();

        double dot = roll.dot(look);
        double cross = roll.x * look.z - roll.z * look.x;
        float angle = (float) Math.toDegrees(Math.atan2(cross, dot));

        int side = 2;
        if (Math.abs(angle) < 45.0F) {
            side = 0;
        } else if (angle >= 45.0F && angle < 135.0F) {
            side = 1;
        } else if (angle <= -45.0F && angle > -135.0F) {
            side = -1;
        }

        this.parkourAbility.dataManager.set(IS_ROLLING, true);
        this.parkourAbility.dataManager.set(ROLL_TICKS, 0);
        this.parkourAbility.dataManager.set(ROLL_DIRECTION, rollDirection);
        this.parkourAbility.dataManager.set(ROLL_SIDE, side);
        this.parkourAbility.dataManager.set(ROLL_SPEED, speed);

        this.parkourAbility.setControlledMovement(player, new Vec3(rollDirection.x * speed, 0.42D, rollDirection.z * speed));

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), player.getSoundSource(), 0.75F, 0.82F);
    }

    public void stopRoll() {
        this.parkourAbility.dataManager.set(IS_ROLLING, false);
        this.parkourAbility.dataManager.set(ROLL_TICKS, 0);
        this.parkourAbility.dataManager.set(ROLL_DIRECTION, Vec3.ZERO);
        this.parkourAbility.dataManager.set(ROLL_SIDE, 0);
        this.parkourAbility.dataManager.set(ROLL_SPEED, 0.0D);

        this.rollCooldown.start(20);
    }

    @Override
    public void reset() {
        this.stopRoll();
        this.rollTicksO = 0;
        this.rollCooldown.timer = 0;
    }

    @Override
    public boolean isActive() {
        return this.parkourAbility.dataManager.get(IS_ROLLING);
    }
}