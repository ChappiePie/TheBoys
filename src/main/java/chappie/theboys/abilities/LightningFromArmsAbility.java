package chappie.theboys.abilities;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.HandSide;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.heroesunited.heroesunited.client.events.HUSetRotationAnglesEvent;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayer;
import xyz.heroesunited.heroesunited.util.HUClientUtil;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;

public class LightningFromArmsAbility extends Ability {

    private boolean shootsFromArm = false;

    public LightningFromArmsAbility() {
        super(TBAbilityTypes.LIGHTNING_FROM_ARMS);
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        if (!JSONUtils.hasField(this.getJsonObject(), "key")) {
            if (shootsFromArm == false) {
                shootsFromArm = true;
            }
        } else {
            JsonObject key = JSONUtils.getJsonObject(this.getJsonObject(), "key");
            if (JSONUtils.getString(key, "pressType").equals("action") && this.cooldownTicks == 0) {
                shootsFromArm = false;
            }
        }
        if (shootsFromArm) {
            HUPlayerUtil.makeLaserLooking(player, 20D);
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
                        shootsFromArm = !shootsFromArm;
                    }
                } else if (pressType.equals("action")) {
                    if (pressed && this.cooldownTicks == 0) {
                        shootsFromArm = true;
                        this.cooldownTicks = JSONUtils.getInt(key, "cooldown", 2);
                    }
                } else if (pressType.equals("held")) {
                    shootsFromArm = pressed;
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (shootsFromArm) {
            double distance = player.getPositionVec().add(0, player.getEyeHeight(), 0).distanceTo(Minecraft.getInstance().objectMouseOver.getHitVec());
            for (int i = 0; i < 3; i++) {
                matrix.push();
                renderer.getEntityModel().translateHand(player.getPrimaryHand(), matrix);
                matrix.scale(0.05F, 0.06F, 0.05F);
                matrix.translate(i * (player.getPrimaryHand() == HandSide.LEFT ? 1 : -1), 10, 0);
                matrix.rotate(new Quaternion(90, 0, 0, true));
                HUClientUtil.renderLightning(player.world.rand, matrix, bufferIn, packedLightIn, distance, i, HUJsonUtils.getColor(this.getJsonObject()));
                matrix.pop();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setRotationAngles(HUSetRotationAnglesEvent event) {
        super.setRotationAngles(event);
        if (shootsFromArm) {
            if (event.getPlayer().getPrimaryHand() == HandSide.RIGHT) {
                event.getPlayerModel().bipedRightArm.rotateAngleX = (float) Math.toRadians(event.getPlayer().rotationPitch - 90);

                event.getPlayerModel().bipedRightArm.rotateAngleY = event.getPlayerModel().bipedHead.rotateAngleY;
                event.getPlayerModel().bipedRightArm.rotateAngleZ = 0;

                event.getPlayerModel().bipedRightArmwear.rotateAngleX = event.getPlayerModel().bipedRightArm.rotateAngleX;
            } else {
                event.getPlayerModel().bipedLeftArm.rotateAngleX = (float) Math.toRadians(event.getPlayer().rotationPitch - 90);

                event.getPlayerModel().bipedLeftArm.rotateAngleY = event.getPlayerModel().bipedHead.rotateAngleY;
                event.getPlayerModel().bipedLeftArm.rotateAngleZ = 0;

                event.getPlayerModel().bipedLeftArmwear.rotateAngleX = event.getPlayerModel().bipedLeftArm.rotateAngleX;
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
