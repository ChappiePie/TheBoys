package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.data.DataAccessor;
import chappie.theboys.TheBoys;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.entity.TrailEntity;
import chappie.theboys.util.interfaces.ILivingEntityEx;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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

public class SpeedAbility extends chappie.modulus.common.ability.SpeedAbility {

    public static final VoxelShape STABLE_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.25D, 16.0D);
    public static final DataAccessor<Integer> TRAIL_DURATION = new DataAccessor<>("trail_duration", DataAccessor.DataSerializer.INT);

    public final IHasTimer.Cooldown crouchCooldown = addCooldown();

    private double xOld, zOld;

    public SpeedAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(TRAIL_DURATION, 10);
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        // 180° turn on double crouch
        if (enabled && !entity.isSwimming() && !entity.isFallFlying()) {
            if (this.conditionManager.test("double_crouch") && this.crouchCooldown.end()) {
                this.crouchCooldown.start(2);
                entity.setYRot(180.0F + entity.getYRot());
                entity.yRotO = entity.getYRot();
                if (entity.getVehicle() != null) {
                    entity.getVehicle().onPassengerTurned(entity);
                }
            }
        }
        super.update(entity, enabled);
    }

    @Override
    protected void applySpeedAttribute(LivingEntity entity, int speedLevel) {
        super.applySpeedAttribute(entity, speedLevel);
        this.setAttribute(entity, this.builder.id, Attributes.ATTACK_SPEED.value(), speedLevel, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        // Feed player periodically
        if (entity instanceof Player player && player.tickCount % 100 == 0) {
            player.getFoodData().eat(1, 1.0F);
        }
    }

    @Override
    protected void resetSpeed(LivingEntity entity) {
        super.resetSpeed(entity);
        this.setAttribute(entity, this.builder.id, Attributes.ATTACK_SPEED.value(), 0.0F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    protected void onMoving(LivingEntity entity, int speedLevel) {
        this.setupTrail(entity, speedLevel);
    }

    @Override
    protected void onSprintCollision(LivingEntity entity, int speedLevel) {
        for (LivingEntity e : entity.level().getEntitiesOfClass(LivingEntity.class,
                CommonUtil.boxWithRange(entity.position(), 0.5D))) {
            if (e != entity) {
                if (entity.level() instanceof ServerLevel level) {
                    e.hurtServer(level, e.damageSources().inWall(), speedLevel);
                }
            }
        }
    }

    @Override
    public int getMaxSpeedLevel() {
        int speedLevel = super.getMaxSpeedLevel();
        TheBoysCap cap = TheBoysCap.getCap(this.entity);
        if (cap != null && cap.compoundV()) {
            speedLevel = (int) (speedLevel * 1.5F);
        }
        return speedLevel;
    }

    @Override
    protected Identifier getAttributeId() {
        return TheBoys.id(this.builder.id);
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
            entity.level().addFreshEntity(new TrailEntity(entity.level(), entity, Color.WHITE, this.dataManager.get(TRAIL_DURATION)));
        }
    }

    public void setAttribute(LivingEntity entity, String name, Attribute attribute, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute));
        Identifier location = TheBoys.id(name);

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
}