package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.data.DataAccessor;
import chappie.theboys.TheBoys;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.entity.TrailEntity;
import chappie.theboys.util.TBCommonUtil;
import chappie.theboys.util.interfaces.ILivingEntityEx;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.awt.*;
import java.util.List;

public class SpeedAbility extends Ability implements IHasTimer {
    public static final VoxelShape STABLE_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.25D, 16.0D);
    public static final DataAccessor<Integer> TRAIL_DURATION = new DataAccessor<>("trail_duration", DataAccessor.DataSerializer.INT);
    public static final DataAccessor<Integer> MAX_SPEED_LVL = new DataAccessor<>("max_speed_lvl", DataAccessor.DataSerializer.INT);
    public static final DataAccessor<Integer> SPEED_LVL = new DataAccessor<>("speed_lvl", DataAccessor.DataSerializer.INT);

    private final Cooldown upgradeCooldown = new Cooldown();
    private final Cooldown cooldown = new Cooldown();

    private double xOld, zOld;

    public SpeedAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(TBCommonUtil.COLOR, Color.BLUE);
        this.dataManager.define(TRAIL_DURATION, 10);
        this.dataManager.define(SPEED_LVL, 1);
        this.dataManager.define(MAX_SPEED_LVL, 10);
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        if (entity.getCommandSenderWorld().isClientSide) return;
        for (Timer timer : this.timers()) {
            timer.update();
        }
        if (enabled && !entity.isSwimming() && !entity.isFallFlying() && entity instanceof ILivingEntityEx ex) {
            int speedLevel = this.dataManager.get(SPEED_LVL);

            double scale = Math.pow(10, 3);
            double x = Math.ceil(ex.theBoys$oldPos().x * scale) / scale;
            double x1 = Math.ceil(entity.getX() * scale) / scale;
            double z = Math.ceil(ex.theBoys$oldPos().z * scale) / scale;
            double z1 = Math.ceil(entity.getZ() * scale) / scale;

            boolean isMoving = x != x1 || z != z1;

            if (entity instanceof Player player && player.tickCount % 100 == 0) {
                player.getFoodData().eat(1, 1.0F);
            }
            this.setAttribute(entity, this.builder.id, Attributes.MOVEMENT_SPEED.value(), speedLevel, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            this.setAttribute(entity, this.builder.id, Attributes.ATTACK_SPEED.value(), speedLevel, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

            if (isMoving && !entity.isPassenger()) {
                this.setupTrail(entity, speedLevel);

                if (this.upgradeCooldown.end() && speedLevel < this.getMaxSpeedLevel()) {
                    this.dataManager.set(SPEED_LVL, speedLevel + 1);
                    this.upgradeCooldown.start(this.dataManager.get(SPEED_LVL) * 10);
                }
            } else {
                if (this.cooldown.end() && speedLevel > 1) {
                    this.dataManager.set(SPEED_LVL, speedLevel - 1);
                    this.cooldown.start(this.dataManager.get(SPEED_LVL));
                }
            }

            if (entity.isSprinting()) {
                /*if (player.isOnGround() && speedLevel > 10 && walkDifference > 1.6F && !player.getAbilities().instabuild) {
                    if (!(Suit.getSuit(player) instanceof SpeedsterSuit)) {
                        player.setSecondsOnFire(10);
                    }
                }*/

                if (speedLevel > 3 && isMoving) {
                    for (LivingEntity e : entity.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class,
                            CommonUtil.boxWithRange(entity.position(), 0.5D))) {
                        if (e != entity) {
                            if (entity.getCommandSenderWorld() instanceof ServerLevel) {
                                e.hurt(e.damageSources().inWall(), speedLevel);
                            }
                        }
                    }
                }
            }
        } else {
            this.dataManager.set(SPEED_LVL, 1);
            this.setAttribute(entity, this.builder.id, Attributes.MOVEMENT_SPEED.value(), 0.0F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            this.setAttribute(entity, this.builder.id, Attributes.ATTACK_SPEED.value(), 0.0F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            this.cooldown.timer = this.upgradeCooldown.timer = 0;
        }
    }

    private void setupTrail(LivingEntity entity, int speedLevel) {
        if (entity.isInvisible() || !(entity instanceof ILivingEntityEx ex)) return;
        float distanceForTrail = 2F - speedLevel / (this.dataManager.get(MAX_SPEED_LVL) * 2F);
        if (this.xOld == 0 || this.zOld == 0) {
            this.xOld = ex.theBoys$oldPos().x;
            this.zOld = ex.theBoys$oldPos().z;
        }
        if (Math.abs(entity.getX() - this.xOld) >= distanceForTrail || Math.abs(entity.getZ() - this.zOld) >= distanceForTrail) {
            this.xOld = ex.theBoys$oldPos().x;
            this.zOld = ex.theBoys$oldPos().z;
            entity.getCommandSenderWorld().addFreshEntity(new TrailEntity(entity.getCommandSenderWorld(), entity, Color.WHITE, this.dataManager.get(TRAIL_DURATION)));
        }
    }

    public int getMaxSpeedLevel() {
        int speedLevel = this.dataManager.get(MAX_SPEED_LVL);
        TheBoysCap cap = TheBoysCap.getCap(this.entity);
        if (cap != null && cap.compoundV()) {
            speedLevel = (int) (speedLevel * 1.5F);
        }
        return speedLevel;
    }

    public void setAttribute(LivingEntity entity, String name, Attribute attribute, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute));
        ResourceLocation location = TheBoys.id(name);

        if (instance != null) {
            var modifier = instance.getModifier(location);
            if (modifier != null && modifier.amount() != amount) {
                instance.removeModifier(location);
                modifier = null;
            }
            if (modifier == null && amount != 0.0F) {
                instance.addTransientModifier(new AttributeModifier(location, amount, operation));
            }
        }
    }

    @Override
    public Iterable<Timer> timers() {
        return List.of(this.cooldown, this.upgradeCooldown);
    }
}
