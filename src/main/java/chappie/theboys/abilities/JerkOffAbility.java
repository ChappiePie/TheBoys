package chappie.theboys.abilities;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.HandSide;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.heroesunited.heroesunited.client.events.HUSetRotationAnglesEvent;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayer;
import xyz.heroesunited.heroesunited.util.HUClientUtil;

import java.util.Random;

public class JerkOffAbility extends Ability {

    private boolean jerkingOff = false;

    public JerkOffAbility() {
        super(TBAbilityTypes.JERK_OFF);
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        if (!JSONUtils.hasField(this.getJsonObject(), "key")) {
            if (jerkingOff == false) {
                jerkingOff = true;
            }
        } else {
            JsonObject key = JSONUtils.getJsonObject(this.getJsonObject(), "key");
            if (JSONUtils.getString(key, "pressType").equals("action") && HUPlayer.getCap(player).getCooldown() == 0) {
                jerkingOff = false;
            }
        }
    }

    @Override
    public void toggle(PlayerEntity player, int id, boolean pressed) {
        if (JSONUtils.hasField(this.getJsonObject(), "key")) {
            JsonObject key = JSONUtils.getJsonObject(this.getJsonObject(), "key");
            String pressType = JSONUtils.getString(key, "pressType", "toggle");

            if (id == JSONUtils.getInt(key, "id")) {
                if (pressType.equals("toggle")) {
                    if (pressed) {
                        jerkingOff = !jerkingOff;
                    }
                } else if (pressType.equals("action")) {
                    if (pressed && HUPlayer.getCap(player).getCooldown() == 0) {
                        jerkingOff = true;
                        HUPlayer.getCap(player).setCooldown(JSONUtils.getInt(key, "cooldown", 2));
                    }
                } else if (pressType.equals("held")) {
                    jerkingOff = pressed;
                }
            }
        }
    }


    @OnlyIn(Dist.CLIENT)
    @Override
    public void setRotationAngles(HUSetRotationAnglesEvent event) {
        if (this.jerkingOff) {
            float f = MathHelper.cos(event.getAgeInTicks()) * 24;
            float rotationX = (float) Math.toRadians(-(event.getPlayer().isCrouching() ? 5F - f : 30F + f));
            if (event.getPlayer().getPrimaryHand() == HandSide.RIGHT) {
                event.getPlayerModel().bipedRightArm.rotateAngleX = rotationX;
                event.getPlayerModel().bipedRightArm.rotateAngleZ = (float) Math.toRadians(-45F);
            } else {
                event.getPlayerModel().bipedLeftArm.rotateAngleX = rotationX;
                event.getPlayerModel().bipedLeftArm.rotateAngleZ = (float) Math.toRadians(45F);
            }
            HUClientUtil.copyAnglesToWear(event.getPlayerModel());
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putBoolean("jerkingOff", this.jerkingOff);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.jerkingOff = nbt.getBoolean("jerkingOff");
    }
}
