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
import net.minecraft.util.DamageSource;
import net.minecraft.util.HandSide;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.heroesunited.heroesunited.client.events.HUSetRotationAnglesEvent;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;
import xyz.heroesunited.heroesunited.common.abilities.IFlyingAbility;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayer;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayerProvider;
import xyz.heroesunited.heroesunited.common.capabilities.IHUPlayer;
import xyz.heroesunited.heroesunited.common.objects.HUAttributes;
import xyz.heroesunited.heroesunited.util.HUClientUtil;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;

import java.awt.*;
import java.util.UUID;

public class FireImmunityAbility extends Ability {

    public FireImmunityAbility() {
        super(TBAbilityTypes.FIRE_IMMUNITY);
    }

    @Mod.EventBusSubscriber(modid = TheBoys.MODID)
    public static class Events {

        @SubscribeEvent
        public static void onBurnDamage(LivingAttackEvent event) {
            if (event.getEntityLiving() instanceof PlayerEntity) {
                for (Ability ability : AbilityHelper.getAbilities((PlayerEntity) event.getEntityLiving())) {
                    if (ability instanceof FireImmunityAbility && event.getSource().isFireDamage()) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}
