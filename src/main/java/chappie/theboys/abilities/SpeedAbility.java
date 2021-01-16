package chappie.theboys.abilities;

import chappie.theboys.TheBoys;
import chappie.theboys.client.render.TrailRenderer;
import chappie.theboys.common.capability.BoysCap;
import chappie.theboys.common.capability.IBoys;
import chappie.theboys.common.entities.TrailEntity;
import chappie.theboys.util.TBUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.abilities.AbilityCreator;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;
import xyz.heroesunited.heroesunited.common.abilities.suit.Suit;
import xyz.heroesunited.heroesunited.common.objects.HUAttributes;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;

import java.awt.*;
import java.util.List;
import java.util.UUID;

public class SpeedAbility extends Ability {

    private int speedLevel;
    private boolean isInSpeed;

    public SpeedAbility() {
        super(TBAbilityTypes.SPEED);
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        super.onUpdate(player);
        if (isInSpeed && player.isSprinting()) {
            float walkedDifference = (player.distanceWalkedModified / 0.6F) - (player.prevDistanceWalkedModified / 0.6F);
            if (!player.world.isRemote && player.ticksExisted % 2 == 0) {
                TrailEntity trail = new TrailEntity(player.world, player, JSONUtils.getInt(this.getJsonObject(), "lifeTimeTrail", 10));
                player.world.addEntity(trail);
            }
            if (player.isOnGround() && speedLevel > 10 && walkedDifference > 1.6F && !player.abilities.isCreativeMode) {
                if (!(Suit.getSuit(player) instanceof SpeedsterSuit)) {
                    player.setFire(10);
                }
            }
            if (!player.isInWater() && player.distanceWalkedModified / 0.6F != player.prevDistanceWalkedModified / 0.6F && player.world.getFluidState(player.getPosition().add(0, -0.1, 0)).isTagged(FluidTags.WATER)) {
                Vector3d vec = player.getMotion();
                player.setMotion(vec.x, 0, vec.z);
                player.fallDistance = 0.0F;
                player.setOnGround(true);
            }

            if (speedLevel > 20) {
                List<Entity> e = player.world.getEntitiesWithinAABBExcludingEntity(player, HUPlayerUtil.getCollisionBoxWithRange(HUPlayerUtil.getPlayerPos(player), 1.0D));
                for (Entity entity : e) {
                    if (entity instanceof LivingEntity) {
                        entity.attackEntityFrom(DamageSource.FALL, 2.0F);
                    }
                }
            }
        }
    }

    @Override
    public void onDeactivated(PlayerEntity player) {
        super.onDeactivated(player);
        resetSpeed(player);
        BoysCap.getCap(player).setSlowMotion(false);
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
            } else if (id == 4) {
                BoysCap.getCap(player).setSlowMotion(!BoysCap.getCap(player).isSlowMotion());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (isInSpeed && player.isSprinting()) {
            TrailRenderer.renderTrail(renderer, player, HUJsonUtils.getColor(this.getJsonObject()));
        }
    }

    protected void increaseDecreaseSpeedLevel(PlayerEntity player, boolean faster) {
        if (isInSpeed) {
            int newSpeedLevel = speedLevel + (faster ? 1 : -1);
            int maxSpeedLevel = JSONUtils.getInt(this.getJsonObject(), "maxSpeedLevel", 10);
            if (newSpeedLevel > (BoysCap.getCap(player).haveCompoundV() ? maxSpeedLevel*1.2 : maxSpeedLevel)  || newSpeedLevel < 1) return;
            setSpeedModifier(player, newSpeedLevel);
        }
    }

    protected void toggleSpeed(PlayerEntity player) {
        if (isInSpeed) {
            resetSpeed(player);
        } else {
            setSpeedModifier(player, 1);
            this.isInSpeed = true;
        }
    }

    protected void resetSpeed(PlayerEntity player) {
        this.speedLevel = 0;
        this.isInSpeed = false;
        if (player.getAttribute(Attributes.MOVEMENT_SPEED) != null && player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(UUID.fromString("ab6f6cc0-4900-45ff-8e91-d35990e79409")) != null) {
            player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(UUID.fromString("ab6f6cc0-4900-45ff-8e91-d35990e79409"));
        }
        if (player.getAttribute(Attributes.ATTACK_SPEED) != null && player.getAttribute(Attributes.ATTACK_SPEED).getModifier(UUID.fromString("ab6f6cc0-4900-45ff-8e91-d35990e79409")) != null) {
            player.getAttribute(Attributes.ATTACK_SPEED).removeModifier(UUID.fromString("ab6f6cc0-4900-45ff-8e91-d35990e79409"));
        }
        player.stepHeight = 0.6F;
    }

    protected void setSpeedModifier(PlayerEntity player, int amount) {
        this.speedLevel = amount;
        TBUtil.setAttribute(player, "Speed", Attributes.MOVEMENT_SPEED, UUID.fromString("ab6f6cc0-4900-45ff-8e91-d35990e79409"), amount, AttributeModifier.Operation.MULTIPLY_TOTAL);
        TBUtil.setAttribute(player, "Speed", Attributes.ATTACK_SPEED, UUID.fromString("ab6f6cc0-4900-45ff-8e91-d35990e79409"), amount, AttributeModifier.Operation.MULTIPLY_TOTAL);
        player.stepHeight = amount != 0 ? (int) MathHelper.clamp(speedLevel, 0, 5F) : 0.6F;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putInt("SpeedLevel", this.speedLevel);
        nbt.putBoolean("isInSpeed", this.isInSpeed);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.speedLevel = nbt.getInt("SpeedLevel");
        this.isInSpeed = nbt.getBoolean("isInSpeed");
    }
}
