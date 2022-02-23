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

    public SpeedAbility(AbilityType type, Player player, JsonObject jsonObject) {
        super(type, player, jsonObject);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register("speedLevel", 0);
    }

    @Override
    public void action(Player player) {
        super.action(player);
        int speedLevel = this.dataManager.<Integer>getValue("speedLevel");
        int amount = speedLevel == 0 ? 1 : speedLevel;

        this.setAttribute(this.player, this.name, Attributes.MOVEMENT_SPEED, getEnabled() ? amount : 0.0F);
        this.setAttribute(this.player, this.name, Attributes.ATTACK_SPEED, getEnabled() ? amount : 0.0F);

        if (this.getEnabled()) {
            float walkDifference = (player.walkDist / 0.6F) - (player.walkDistO / 0.6F);

            if (player.zza > 0.0F && walkDifference > 0.0F && !player.level.isClientSide && player.tickCount % 2 == 0) {
                TrailEntity trail = new TrailEntity(player.level, player, HUJsonUtils.getColor(this.getJsonObject()), GsonHelper.getAsInt(this.getJsonObject(), "lifeTimeTrail", 20));
                player.level.addFreshEntity(trail);
            }

            if (player.isSprinting()) {
                if (player.isOnGround() && speedLevel > 10 && walkDifference > 1.6F && !player.getAbilities().instabuild) {
                    if (!(Suit.getSuit(player) instanceof SpeedsterSuit)) {
                        player.setSecondsOnFire(10);
                    }
                }

                if (speedLevel > 5 && walkDifference > 0.6F) {
                    for (LivingEntity entity : player.level.getEntitiesOfClass(LivingEntity.class,
                            HUPlayerUtil.getCollisionBoxWithRange(player.position(), 1.0D))) {
                        if (entity != player) {
                            entity.hurt(DamageSource.IN_WALL, speedLevel);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setEnabled(Player player, boolean enabled) {
        super.setEnabled(player, enabled);
    }

    public void increaseDecreaseSpeedLevel(double faster) {
        int max = GsonHelper.getAsInt(this.getJsonObject(), "maxSpeedLevel", 10);
        int speedLevel = (int) (this.dataManager.<Integer>getValue("speedLevel") + faster);
        if (speedLevel <= (BoysCap.getCap(this.player).haveCompoundV() ? max * 1.25F : max) && speedLevel > 1) {
            this.dataManager.set("speedLevel", speedLevel);
        }
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
