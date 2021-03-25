package chappie.theboys.abilities;

import chappie.theboys.common.capability.BoysCap;
import chappie.theboys.common.entities.LightningProjectile;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.HandSide;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.heroesunited.heroesunited.client.events.HUSetRotationAnglesEvent;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.util.HUClientUtil;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;

public class LightningProjectileAbility extends Ability {

    private boolean shootsFromArm = false;

    public LightningProjectileAbility() {
        super(TBAbilityTypes.LIGHTNING_PROJECTILE);
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        if (!JSONUtils.isValidNode(this.getJsonObject(), "key")) {
            if (shootsFromArm == false) {
                shootsFromArm = true;
            }
        } else {
            JsonObject key = JSONUtils.getAsJsonObject(this.getJsonObject(), "key");
            if (JSONUtils.getAsString(key, "pressType").equals("action") && this.cooldownTicks == 0) {
                shootsFromArm = false;
            }
        }
        if (shootsFromArm && !player.level.isClientSide) {
            LightningProjectile entity = new LightningProjectile(player.level, HUJsonUtils.getColor(this.getJsonObject()));
            entity.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 1.5F, 1.0F);
            player.level.addFreshEntity(entity);
        }
    }

    @Override
    public void toggle(PlayerEntity player, int id, boolean pressed) {
        if (JSONUtils.isValidNode(this.getJsonObject(), "key")) {
            JsonObject key = JSONUtils.getAsJsonObject(this.getJsonObject(), "key");
            String pressType = JSONUtils.getAsString(key, "pressType", "toggle");

            if (id == JSONUtils.getAsInt(key, "id")) {
                if (pressType.equals("toggle")) {
                    if (pressed) {
                        shootsFromArm = !shootsFromArm;
                    }
                } else if (pressType.equals("action")) {
                    if (pressed && this.cooldownTicks == 0) {
                        shootsFromArm = true;
                        int cooldown = JSONUtils.getAsInt(key, "cooldown", 2);
                        this.cooldownTicks = BoysCap.getCap(player).haveCompoundV() ? cooldown / 2 : cooldown;
                    }
                } else if (pressType.equals("held")) {
                    shootsFromArm = pressed;
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setRotationAngles(HUSetRotationAnglesEvent event) {
        super.setRotationAngles(event);
        if (shootsFromArm) {
            if (event.getPlayer().getMainArm() == HandSide.RIGHT) {
                event.getPlayerModel().rightArm.xRot = (float) Math.toRadians(event.getPlayer().xRot - 90);

                event.getPlayerModel().rightArm.yRot = event.getPlayerModel().head.yRot;
                event.getPlayerModel().rightArm.zRot = 0;

                event.getPlayerModel().rightSleeve.xRot = event.getPlayerModel().rightArm.xRot;
            } else {
                event.getPlayerModel().leftArm.xRot = (float) Math.toRadians(event.getPlayer().xRot - 90);

                event.getPlayerModel().leftArm.yRot = event.getPlayerModel().head.yRot;
                event.getPlayerModel().leftArm.zRot = 0;

                event.getPlayerModel().leftSleeve.xRot = event.getPlayerModel().leftArm.xRot;
            }
        }
        HUClientUtil.copyAnglesToWear(event.getPlayerModel());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderFirstPersonArm(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, HandSide side) {
        if (shootsFromArm) {
            HUClientUtil.drawArmWithLightning(matrix, bufferIn, renderer, player, side, 4, packedLightIn, HUJsonUtils.getColor(this.getJsonObject()));
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putBoolean("shootsFromArm", this.shootsFromArm);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.shootsFromArm = nbt.getBoolean("shootsFromArm");
    }
}
