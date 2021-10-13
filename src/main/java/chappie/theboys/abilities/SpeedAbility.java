package chappie.theboys.abilities;

import chappie.theboys.client.render.TrailRenderer;
import chappie.theboys.common.capability.BoysCap;
import chappie.theboys.common.entities.TrailEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Blocks;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;
import xyz.heroesunited.heroesunited.common.abilities.suit.Suit;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;

import java.util.List;
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
    public void onUpdate(PlayerEntity player) {
        super.onUpdate(player);
        if (this.dataManager.<Boolean>getValue("enabled") && player.isSprinting()) {
            float walkedDifference = (player.walkDist / 0.6F) - (player.walkDistO / 0.6F);
            if (!player.level.isClientSide && player.tickCount % 2 == 0) {
                TrailEntity trail = new TrailEntity(player.level, player, JSONUtils.getAsInt(this.getJsonObject(), "lifeTimeTrail", 10));
                player.level.addFreshEntity(trail);
            }
            if (player.isOnGround() && this.dataManager.<Integer>getValue("speed_level") > 10 && walkedDifference > 1.6F && !player.abilities.instabuild) {
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
    public void onDeactivated(PlayerEntity player) {
        super.onDeactivated(player);
        resetSpeed(player);
    }

    @Override
    public void toggle(PlayerEntity player, int id, boolean pressed) {
        if (pressed) {
            if (id == 1) {
                toggleSpeed(player);
            } else if (id == 2) {
                increaseDecreaseSpeedLevel(player, true);
            } else if (id == 3) {
                increaseDecreaseSpeedLevel(player, false);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (this.dataManager.<Boolean>getValue("enabled") && player.isSprinting()) {
            TrailRenderer.renderTrail(renderer, player, HUJsonUtils.getColor(this.getJsonObject()));
        }
    }

    protected void increaseDecreaseSpeedLevel(PlayerEntity player, boolean faster) {
        if (this.dataManager.<Boolean>getValue("enabled")) {
            int newSpeedLevel = this.dataManager.<Integer>getValue("speed_level") + (faster ? 1 : -1);
            int maxSpeedLevel = JSONUtils.getAsInt(this.getJsonObject(), "maxSpeedLevel", 10);
            if (newSpeedLevel > (BoysCap.getCap(player).haveCompoundV() ? maxSpeedLevel*1.2 : maxSpeedLevel)  || newSpeedLevel < 1) return;
            setSpeedModifier(player, newSpeedLevel);
        }
    }

    protected void toggleSpeed(PlayerEntity player) {
        if (this.dataManager.<Boolean>getValue("enabled")) {
            resetSpeed(player);
        } else {
            setSpeedModifier(player, 1);
            this.dataManager.set("enabled", true);
        }
    }

    protected void resetSpeed(PlayerEntity player) {
        this.dataManager.set("speed_level", 0);
        this.dataManager.set("enabled", false);
        AbilityHelper.setAttribute(player, "Speed", Attributes.MOVEMENT_SPEED, UUID.fromString("ab6f6cc0-4900-45ff-8e91-d35990e79409"), 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        AbilityHelper.setAttribute(player, "Speed", Attributes.ATTACK_SPEED, UUID.fromString("ab6f6cc0-4900-45ff-8e91-d35990e79409"), 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        player.maxUpStep = 0.6F;
    }

    protected void setSpeedModifier(PlayerEntity player, int amount) {
        this.dataManager.set("speed_level", amount);
        setAttribute(player, "Speed", Attributes.MOVEMENT_SPEED, UUID.fromString("ab6f6cc0-4900-45ff-8e91-d35990e79409"), amount, AttributeModifier.Operation.MULTIPLY_TOTAL);
        setAttribute(player, "Speed", Attributes.ATTACK_SPEED, UUID.fromString("ab6f6cc0-4900-45ff-8e91-d35990e79409"), amount, AttributeModifier.Operation.MULTIPLY_TOTAL);
        player.maxUpStep = amount != 0 ? (int) MathHelper.clamp(this.dataManager.<Integer>getValue("speed_level"), 0, 5F) : 0.6F;
    }

    public void setAttribute(LivingEntity entity, String name, Attribute attribute, UUID uuid, double amount, AttributeModifier.Operation operation) {
        ModifiableAttributeInstance instance = entity.getAttribute(attribute);

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
