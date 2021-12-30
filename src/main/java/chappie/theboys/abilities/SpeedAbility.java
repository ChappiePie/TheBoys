package chappie.theboys.abilities;

import chappie.theboys.common.capability.BoysCap;
import chappie.theboys.common.entities.TrailEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.abilities.suit.Suit;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpeedAbility extends Ability {

    public SpeedAbility() {
        super(TBAbilityTypes.SPEED);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register("enabled", false);
        this.dataManager.register("speed_level", 0);
    }

    @Override
    public void onUpdate(Player player) {
        super.onUpdate(player);
        if (this.dataManager.<Boolean>getValue("enabled") && player.isSprinting() && !player.isCrouching()) {
            float walkedDifference = (player.walkDist / 0.6F) - (player.walkDistO / 0.6F);
            if (!player.level.isClientSide && player.tickCount % 2 == 0) {
                TrailEntity trail = new TrailEntity(player.level, player, HUJsonUtils.getColor(this.getJsonObject()), GsonHelper.getAsInt(this.getJsonObject(), "lifeTimeTrail", 20));
                player.level.addFreshEntity(trail);
            }
            if (player.isOnGround() && this.dataManager.<Integer>getValue("speed_level") > 10 && walkedDifference > 1.6F && !player.getAbilities().instabuild) {
                if (!(Suit.getSuit(player) instanceof SpeedsterSuit)) {
                    player.setSecondsOnFire(10);
                }
            }
            if (!player.isInWater() && player.walkDist / 0.6F != player.walkDistO / 0.6F) {
                if (player.level.getBlockState(new BlockPos(player.position().add(0, -0.3, 0))).is(Blocks.WATER)) {
                    player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
                    player.fallDistance = 0.0F;
                    player.setOnGround(true);
                }
            }

            if (this.dataManager.<Integer>getValue("speed_level") > 20) {
                List<Entity> e = player.level.getEntities(player, HUPlayerUtil.getCollisionBoxWithRange(HUPlayerUtil.getPlayerPos(player), 1.0D));
                for (Entity entity : e) {
                    if (entity instanceof LivingEntity) {
                        entity.hurt(DamageSource.FALL, 2.0F);
                    }
                }
            }
        }
    }

    @Override
    public void onDeactivated(Player player) {
        super.onDeactivated(player);
        resetSpeed(player);
    }

    @Override
    public void onKeyInput(Player player, Map<Integer, Boolean> map) {
        super.onKeyInput(player, map);
        if (map.get(1)) {
            toggleSpeed(player);
        } else if (map.get(2)) {
            increaseDecreaseSpeedLevel(player, true);
        } else if (map.get(3)) {
            increaseDecreaseSpeedLevel(player, false);
        }
    }

    protected void increaseDecreaseSpeedLevel(Player player, boolean faster) {
        if (this.dataManager.<Boolean>getValue("enabled")) {
            int newSpeedLevel = this.dataManager.<Integer>getValue("speed_level") + (faster ? 1 : -1);
            int maxSpeedLevel = GsonHelper.getAsInt(this.getJsonObject(), "maxSpeedLevel", 10);
            if (newSpeedLevel > (BoysCap.getCap(player).haveCompoundV() ? maxSpeedLevel*1.2 : maxSpeedLevel)  || newSpeedLevel < 1) return;
            setSpeedModifier(player, newSpeedLevel);
        }
    }

    protected void toggleSpeed(Player player) {
        if (this.dataManager.<Boolean>getValue("enabled")) {
            resetSpeed(player);
        } else {
            setSpeedModifier(player, 1);
            this.dataManager.set("enabled", true);
        }
    }

    protected void resetSpeed(Player player) {
        this.dataManager.set("speed_level", 0);
        this.dataManager.set("enabled", false);
        player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(UUID.fromString("ab6f6cc0-4900-45ff-8e91-d35990e79409"));
        player.getAttribute(Attributes.ATTACK_SPEED).removeModifier(UUID.fromString("ab6f6cc0-4900-45ff-8e91-d35990e79409"));
        player.maxUpStep = 1F;
    }

    protected void setSpeedModifier(Player player, int amount) {
        this.dataManager.set("speed_level", amount);
        setAttribute(player, "Speed", Attributes.MOVEMENT_SPEED, UUID.fromString("ab6f6cc0-4900-45ff-8e91-d35990e79409"), amount, AttributeModifier.Operation.MULTIPLY_TOTAL);
        setAttribute(player, "Speed", Attributes.ATTACK_SPEED, UUID.fromString("ab6f6cc0-4900-45ff-8e91-d35990e79409"), amount, AttributeModifier.Operation.MULTIPLY_TOTAL);
        player.maxUpStep = amount != 0 ? (int) Mth.clamp(this.dataManager.<Integer>getValue("speed_level"), 0, 5F) : 1F;
    }

    public void setAttribute(LivingEntity entity, String name, Attribute attribute, UUID uuid, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(attribute);

        if (instance == null || entity.level.isClientSide) {
            return;
        }

        AttributeModifier modifier = instance.getModifier(uuid);

        if (amount == 0 || modifier != null && (modifier.getAmount() != amount || modifier.getOperation() != operation)) {
            instance.removeModifier(uuid);
        }

        modifier = instance.getModifier(uuid);

        if (modifier == null) {
            modifier = new AttributeModifier(uuid, name, amount, operation);
            instance.addTransientModifier(modifier);
        }
    }
}
