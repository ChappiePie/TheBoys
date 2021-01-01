package chappie.theboys.abilities;

import chappie.theboys.TheBoys;
import chappie.theboys.util.TBClientUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.heroesunited.heroesunited.client.events.HUSetRotationAnglesEvent;
import xyz.heroesunited.heroesunited.common.abilities.*;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayer;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayerProvider;
import xyz.heroesunited.heroesunited.common.capabilities.IHUPlayer;
import xyz.heroesunited.heroesunited.common.objects.HUAttributes;
import xyz.heroesunited.heroesunited.util.HUClientUtil;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;

import java.awt.*;
import java.util.UUID;

public class LasersFromEyesAbility extends Ability {

    public LasersFromEyesAbility() {
        super(TBAbilityTypes.LASERS_FROM_EYES);
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        IHUPlayer cap = HUPlayer.getCap(player);
        if (cap.getType() == 1) {
            HUPlayerUtil.makeLaserLooking(player, 40D);
        }
    }

    @Override
    public void toggle(PlayerEntity player, int id, boolean pressed) {
        IHUPlayer cap = HUPlayer.getCap(player);
        if (id == 1) {
            cap.set("ShootsFromEyes", pressed);
        } else if (id == 5 && getSuperpower() == "theboys:homelander") {
            cap.setType(cap.getType() == 0 ? 2 : 0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setRotationAngles(HUSetRotationAnglesEvent event) {
        event.getPlayer().getCapability(HUPlayerProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.getType() == 2) {
                float f = event.getPlayer().ticksExisted + event.getAgeInTicks();
                float rotationX = (float) Math.toRadians(-(45F + (MathHelper.cos(f))));
                if (event.getPlayer().getPrimaryHand() == HandSide.RIGHT) {
                    event.getPlayerModel().bipedRightArm.rotateAngleX = rotationX;
                    event.getPlayerModel().bipedRightArm.rotateAngleZ = (float) Math.toRadians(-45F);
                } else {
                    event.getPlayerModel().bipedLeftArm.rotateAngleX = rotationX;
                    event.getPlayerModel().bipedLeftArm.rotateAngleZ = (float) Math.toRadians(45F);
                }
                HUClientUtil.copyAnglesToWear(event.getPlayerModel());
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        JsonArray jsonColor = JSONUtils.getJsonArray(this.getJsonObject(), "color");
        if (jsonColor.size() != 4) throw new JsonParseException("The color must contain 4 entries, one for each color!");
        Color color = new Color(jsonColor.get(0).getAsFloat() / 255F, jsonColor.get(1).getAsFloat() / 255F, jsonColor.get(2).getAsFloat() / 255F, jsonColor.get(3).getAsFloat() / 255F);
        if (HUPlayer.getCap(player).getFromName("ShootsFromEyes").equals(true)) {
            TBClientUtil.renderHeatvision(renderer, matrix, bufferIn, packedLightIn, player, true, color);
            TBClientUtil.renderHeatvision(renderer, matrix, bufferIn, packedLightIn, player, false, color);
        }
    }

    @Override
    public void onDeactivated(PlayerEntity player) {
        super.onDeactivated(player);
        HUPlayer.getCap(player).set("ShootsFromEyes", false);
    }
}
