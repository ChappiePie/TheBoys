package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.data.DataAccessor;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.entity.TrailEntity;
import chappie.theboys.util.TBCommonUtil;
import chappie.theboys.util.interfaces.ILivingEntityEx;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.awt.*;
import java.util.UUID;

public class SpeedAbility extends Ability {
    public static final VoxelShape STABLE_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.25D, 16.0D);
    public static final DataAccessor<Integer> TRAIL_DURATION = new DataAccessor<>("trail_duration", DataAccessor.DataSerializer.INT);
    public static final DataAccessor<Integer> MAX_SPEED_LVL = new DataAccessor<>("max_speed_lvl", DataAccessor.DataSerializer.INT);
    public static final DataAccessor<Integer> SPEED_LVL = new DataAccessor<>("speed_lvl", DataAccessor.DataSerializer.INT);
    private int upgradeCooldown, cooldown;

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
            this.setAttribute(entity, this.builder.id, Attributes.MOVEMENT_SPEED, speedLevel, AttributeModifier.Operation.MULTIPLY_TOTAL);
            this.setAttribute(entity, this.builder.id, Attributes.ATTACK_SPEED, speedLevel, AttributeModifier.Operation.MULTIPLY_TOTAL);

            if (isMoving && !entity.isPassenger()) {
                this.setupTrail(entity, speedLevel);

                if (this.upgradeCooldown > 0) {
                    --this.upgradeCooldown;
                } else {
                    if (speedLevel < this.getMaxSpeedLevel()) {
                        this.dataManager.set(SPEED_LVL, speedLevel + 1);
                        this.upgradeCooldown = this.dataManager.get(SPEED_LVL) * 10;
                    }
                }
            } else {
                if (this.cooldown > 0) {
                    --this.cooldown;
                } else {
                    if (speedLevel > 1) {
                        this.dataManager.set(SPEED_LVL, speedLevel - 1);
                        this.cooldown = this.dataManager.get(SPEED_LVL);
                    }
                }
            }

            if (entity.isSprinting()) {
                /*if (player.isOnGround() && speedLevel > 10 && walkDifference > 1.6F && !player.getAbilities().instabuild) {
                    if (!(Suit.getSuit(player) instanceof SpeedsterSuit)) {
                        player.setSecondsOnFire(10);
                    }
                }*/

                if (speedLevel > 5 && isMoving) {
                    for (LivingEntity e : entity.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class,
                            CommonUtil.boxWithRange(entity.position(), 0.5D))) {
                        if (e != entity) {
                            e.hurt(e.damageSources().inWall(), speedLevel);
                        }
                    }
                }
            }
        } else {
            this.dataManager.set(SPEED_LVL, 1);
            this.setAttribute(entity, this.builder.id, Attributes.MOVEMENT_SPEED, 0.0F, AttributeModifier.Operation.MULTIPLY_TOTAL);
            this.setAttribute(entity, this.builder.id, Attributes.ATTACK_SPEED, 0.0F, AttributeModifier.Operation.MULTIPLY_TOTAL);
            this.cooldown = this.upgradeCooldown = 0;
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
        AttributeInstance instance = entity.getAttribute(attribute);
        var uuid = UUID.fromString("fefb466b-f73a-4e1d-8bac-77f702d2b437");

        if (instance != null) {
            var modifier = instance.getModifier(uuid);
            if (modifier != null && modifier.getAmount() != amount) {
                instance.removeModifier(uuid);
                modifier = null;
            }
            if (modifier == null && amount != 0.0F) {
                instance.addTransientModifier(new AttributeModifier(uuid, name, amount, operation));
            }
        }
    }
}
