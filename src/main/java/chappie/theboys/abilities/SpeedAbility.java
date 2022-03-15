package chappie.theboys.abilities;

import chappie.theboys.common.capability.BoysCap;
import chappie.theboys.common.entities.TrailEntity;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;
import xyz.heroesunited.heroesunited.common.abilities.AbilityType;
import xyz.heroesunited.heroesunited.common.abilities.JSONAbility;
import xyz.heroesunited.heroesunited.common.abilities.suit.Suit;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;

import java.util.UUID;

public class SpeedAbility extends JSONAbility {
    public static final VoxelShape STABLE_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.25D, 16.0D);
    private int upgradeCooldown, cooldown;

    public SpeedAbility(AbilityType type, Player player, JsonObject jsonObject) {
        super(type, player, jsonObject);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register("speedLevel", 1);
    }

    @Override
    public void action(Player player) {
        super.action(player);
        if (this.getEnabled()) {
            int speedLevel = this.dataManager.getAsInt("speedLevel");
            float walkDifference = (player.walkDist / 0.6F) - (player.walkDistO / 0.6F);

            this.setAttribute(this.player, this.name, Attributes.MOVEMENT_SPEED, speedLevel);
            this.setAttribute(this.player, this.name, Attributes.ATTACK_SPEED, speedLevel);

            if (walkDifference > 0.0F && !player.level.isClientSide) {
                TrailEntity trail = new TrailEntity(player.level, player, HUJsonUtils.getColor(this.getJsonObject()), GsonHelper.getAsInt(this.getJsonObject(), "lifeTimeTrail", 20));
                player.level.addFreshEntity(trail);
            }

            if (walkDifference > 0.0F) {
                if (this.upgradeCooldown > 0) {
                    --this.upgradeCooldown;
                } else {
                    if (speedLevel < this.getMaxSpeedLevel()) {
                        this.dataManager.set("speedLevel", speedLevel + 1);
                        this.upgradeCooldown = this.dataManager.getAsInt("speedLevel") * 5;
                    }
                }
            } else {
                if (this.cooldown > 0) {
                    --this.cooldown;
                } else {
                    if (speedLevel > 1) {
                        this.dataManager.set("speedLevel", speedLevel - 1);
                        this.upgradeCooldown = 0;
                        this.cooldown = this.dataManager.getAsInt("speedLevel");
                    }
                }
            }

            if (player.isSprinting()) {
                if (player.isOnGround() && speedLevel > 10 && walkDifference > 1.6F && !player.getAbilities().instabuild) {
                    if (!(Suit.getSuit(player) instanceof SpeedsterSuit)) {
                        player.setSecondsOnFire(10);
                    }
                }

                if (speedLevel > 5 && walkDifference > 0.0F) {
                    for (LivingEntity entity : player.level.getEntitiesOfClass(LivingEntity.class,
                            HUPlayerUtil.getCollisionBoxWithRange(player.position(), 1.5D))) {
                        if (entity != player) {
                            entity.hurt(DamageSource.IN_WALL, speedLevel);
                        }
                    }
                }
            }
        } else {
            this.dataManager.set("speedLevel", 1);
            this.setAttribute(this.player, this.name, Attributes.MOVEMENT_SPEED, 0.0F);
            this.setAttribute(this.player, this.name, Attributes.ATTACK_SPEED, 0.0F);
            this.cooldown = this.upgradeCooldown = 0;
        }
    }

    public int getMaxSpeedLevel() {
        int speedLevel = GsonHelper.getAsInt(this.getJsonObject(), "maxSpeedLevel", 10);
        if (BoysCap.getCap(this.player).haveCompoundV()) {
            speedLevel *= 1.5F;
        }
        return speedLevel;
    }

    public void setAttribute(LivingEntity entity, String name, Attribute attribute, double amount) {
        AttributeInstance instance = entity.getAttribute(attribute);
        var uuid = UUID.fromString("fefb466b-f73a-4e1d-8bac-77f702d2b437");

        if (instance != null) {
            if (instance.getModifier(uuid) != null) {
                instance.removeModifier(uuid);
            }
            if (amount != 0.0F) {
                instance.addTransientModifier(new AttributeModifier(uuid, name, amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
    }
}
