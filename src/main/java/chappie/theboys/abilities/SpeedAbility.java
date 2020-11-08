package chappie.theboys.abilities;

import chappie.theboys.client.render.TrailRenderer;
import chappie.theboys.common.capability.BoysCap;
import chappie.theboys.common.capability.IBoys;
import chappie.theboys.common.entities.TrailEntity;
import chappie.theboys.abilities.suits.ISpeedSuit;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;
import xyz.heroesunited.heroesunited.common.abilities.suit.Suit;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;

import java.awt.*;
import java.util.List;
import java.util.UUID;

public abstract class SpeedAbility extends TBAbility {

    public void onUpdate(PlayerEntity player) {
        super.onUpdate(player);
        IBoys boys = BoysCap.getCap(player);
        if (boys.isInSpeed()) {
            boolean isMoving = player.distanceWalkedModified / 0.6F != player.prevDistanceWalkedModified / 0.6F;
            float walkedDifference = (player.distanceWalkedModified / 0.6F) - (player.prevDistanceWalkedModified / 0.6F);
            if (!player.world.isRemote && player.ticksExisted % 2 == 0 && isMoving) {
                TrailEntity trail = new TrailEntity(player.world, player, getLifeTimeForTrail());
                player.world.addEntity(trail);
            }
            if (player.isSprinting()) {
                if (player.isOnGround() && boys.getSpeedLevel() > 10 && walkedDifference > 1.6F && !player.abilities.isCreativeMode) {
                    if (!(Suit.getSuit(player) instanceof ISpeedSuit)) {
                        player.setFire(10);
                    }
                }
                if (!player.isInWater() && isMoving && player.world.getFluidState(player.getPosition().add(0, -0.1, 0)).isTagged(FluidTags.WATER)) {
                    Vector3d vec = player.getMotion();
                    player.setMotion(vec.x, 0, vec.z);
                    player.fallDistance = 0.0F;
                    player.setOnGround(true);
                }

                if (boys.getSpeedLevel() > 20) {
                    List<Entity> e = player.world.getEntitiesWithinAABBExcludingEntity(player, HUPlayerUtil.getCollisionBoxWithRange(HUPlayerUtil.getPlayerPos(player), 1.0D));
                    for (Entity entity : e) {
                        if (!e.isEmpty() && entity instanceof LivingEntity) {
                            entity.attackEntityFrom(DamageSource.FALL, 2.0F);
                        }
                    }
                }
            }
        }
    }

    public void onDeactivated(PlayerEntity player) {
        super.onDeactivated(player);
        resetSpeed(player);
    }

    public void toggle(PlayerEntity player, int id) {
        switch (id) {
            case 1:
                toggleSpeed(player);
                break;
            case 2:
                increaseDecreaseSpeedLevel(player, true);
                break;
            case 3:
                increaseDecreaseSpeedLevel(player, false);
                break;
            case 4:
                BoysCap.getCap(player).setSlowMotion(!BoysCap.getCap(player).isSlowMotion());
                break;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (BoysCap.getCap(player).isInSpeed() && player.distanceWalkedModified / 0.6F != player.prevDistanceWalkedModified / 0.6F) {
            TrailRenderer.renderTrail(renderer, player, getTrailColor());
        }
    }

    public abstract int getLifeTimeForTrail();

    public abstract Color getTrailColor();

    public abstract int getMaxSpeedLevel(PlayerEntity player);

    protected void increaseDecreaseSpeedLevel(PlayerEntity player, boolean faster) {
        if (BoysCap.getCap(player).isInSpeed()) {
            int speedLevel = BoysCap.getCap(player).getSpeedLevel() + (faster ? 1 : -1);
            if (speedLevel > getMaxSpeedLevel(player) || speedLevel < 1)
                return;
            setSpeedModifier(player, speedLevel);
        }
    }

    protected void toggleSpeed(PlayerEntity player) {
        if (BoysCap.getCap(player).isInSpeed()) {
            resetSpeed(player);
        } else {
            setSpeedModifier(player, 1);
            BoysCap.getCap(player).setInSpeed(true);
        }
    }

    protected void resetSpeed(PlayerEntity player) {
        setSpeedModifier(player, 0);
        BoysCap.getCap(player).setInSpeed(false);
    }

    protected void setSpeedModifier(PlayerEntity player, int amount) {
        BoysCap.getCap(player).setSpeedLevel(amount);
        AbilityHelper.setAttribute(player, "Speed", Attributes.MOVEMENT_SPEED, UUID.fromString("ab6f6cc0-4900-45ff-8e91-d35990e79409"), BoysCap.getCap(player).getSpeedLevel(), AttributeModifier.Operation.MULTIPLY_TOTAL);
        AbilityHelper.setAttribute(player, "Speed", Attributes.ATTACK_SPEED, UUID.fromString("ab6f6cc0-4900-45ff-8e91-d35990e79409"), BoysCap.getCap(player).getSpeedLevel(), AttributeModifier.Operation.MULTIPLY_TOTAL);
        int stepheight = (int) MathHelper.clamp(BoysCap.getCap(player).getSpeedLevel(), 0, 5F);
        player.stepHeight = amount != 0 ? stepheight : 0.6F;
    }
}
