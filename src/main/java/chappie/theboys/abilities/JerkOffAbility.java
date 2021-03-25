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
import xyz.heroesunited.heroesunited.util.HUClientUtil;

public class JerkOffAbility extends Ability {

    private boolean jerkingOff = false;

    public JerkOffAbility() {
        super(TBAbilityTypes.JERK_OFF);
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        if (!JSONUtils.isValidNode(this.getJsonObject(), "key")) {
            if (jerkingOff == false) {
                jerkingOff = true;
            }
        } else {
            JsonObject key = JSONUtils.getAsJsonObject(this.getJsonObject(), "key");
            if (JSONUtils.getAsString(key, "pressType").equals("action") && this.cooldownTicks == 0) {
                jerkingOff = false;
            }
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
                        jerkingOff = !jerkingOff;
                    }
                } else if (pressType.equals("action")) {
                    if (pressed && this.cooldownTicks == 0) {
                        jerkingOff = true;
                        this.cooldownTicks = JSONUtils.getAsInt(key, "cooldown", 2);
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
            if (event.getPlayer().getMainArm() == HandSide.RIGHT) {
                event.getPlayerModel().rightArm.xRot = rotationX;
                event.getPlayerModel().rightArm.zRot = (float) Math.toRadians(-45F);
            } else {
                event.getPlayerModel().leftArm.xRot = rotationX;
                event.getPlayerModel().leftArm.zRot = (float) Math.toRadians(45F);
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
