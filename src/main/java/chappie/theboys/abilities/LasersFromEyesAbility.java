package chappie.theboys.abilities;

import chappie.theboys.util.TBClientUtil;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;

public class LasersFromEyesAbility extends Ability {

    private boolean shootsFromEyes = false;

    public LasersFromEyesAbility() {
        super(TBAbilityTypes.LASERS_FROM_EYES);
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        if (!JSONUtils.isValidNode(this.getJsonObject(), "key")) {
            if (shootsFromEyes == false) {
                shootsFromEyes = true;
            }
        } else {
            JsonObject key = JSONUtils.getAsJsonObject(this.getJsonObject(), "key");
            if (JSONUtils.getAsString(key, "pressType").equals("action") && this.cooldownTicks == 0) {
                shootsFromEyes = false;
            }
        }

        if (this.shootsFromEyes) {
            HUPlayerUtil.makeLaserLooking(player, 40D);
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
                        shootsFromEyes = !shootsFromEyes;
                    }
                } else if (pressType.equals("action")) {
                    if (pressed && this.cooldownTicks == 0) {
                        shootsFromEyes = true;
                        this.cooldownTicks = JSONUtils.getAsInt(key, "cooldown", 2);
                    }
                } else if (pressType.equals("held")) {
                    shootsFromEyes = pressed;
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (this.shootsFromEyes) {
            TBClientUtil.renderHeatvision(renderer, matrix, bufferIn, packedLightIn, player, true, HUJsonUtils.getColor(this.getJsonObject()));
            TBClientUtil.renderHeatvision(renderer, matrix, bufferIn, packedLightIn, player, false, HUJsonUtils.getColor(this.getJsonObject()));
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putBoolean("ShootsFromEyes", this.shootsFromEyes);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.shootsFromEyes = nbt.getBoolean("ShootsFromEyes");
    }
}
