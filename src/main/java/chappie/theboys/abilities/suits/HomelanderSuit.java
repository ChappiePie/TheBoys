package chappie.theboys.abilities.suits;

import chappie.theboys.TheBoys;
import chappie.theboys.abilities.TBAbilityTypes;
import chappie.theboys.common.items.TBItems;
import chappie.theboys.util.TBArmorMaterial;
import chappie.theboys.util.TBClientUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.heroesunited.heroesunited.client.events.HUSetRotationAnglesEvent;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;
import xyz.heroesunited.heroesunited.common.abilities.suit.SuitItem;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayer;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.minecraft.inventory.EquipmentSlotType.*;

public class HomelanderSuit extends TheSevenSuit {

    public HomelanderSuit() {
        super(TBSuitTypes.HOMELANDER);
    }

    public IArmorMaterial getSuitMaterial() {
        return TBArmorMaterial.HOMELANDER;
    }

    @Override
    public Item getChestplate() {
        return TBItems.HOMELANDER_CHEST;
    }

    @Override
    public Item getLegs() {
        return TBItems.HOMELANDER_LEGS;
    }

    @Override
    public Item getBoots() {
        return TBItems.HOMELANDER_BOOTS;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderLayer(@Nullable LivingRenderer<? extends LivingEntity, ? extends EntityModel<?>> entityRenderer, @Nullable LivingEntity entity, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        AtomicBoolean haveNullRotation = new AtomicBoolean(false);
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            haveNullRotation.set(AbilityHelper.getEnabled(TBAbilityTypes.HOMELANDER, player) && HUPlayer.getCap(player).isFlying() && !player.isOnGround() && !player.isSwimming() && player.isSprinting());
        }
        TBClientUtil.renderCape(entityRenderer, matrix, bufferIn, packedLightIn, entity, partialTicks, new ResourceLocation(TheBoys.MODID, "textures/suits/homelander/cape.png"), haveNullRotation.get());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setRotationAngles(HUSetRotationAnglesEvent event) {
        if (event.getPlayer().getItemStackFromSlot(CHEST).getItem() instanceof SuitItem) {
            event.getPlayerModel().bipedBodyWear.showModel = false;
            event.getPlayerModel().bipedRightArmwear.showModel = false;
            event.getPlayerModel().bipedLeftArmwear.showModel = false;
        }

        if (event.getPlayer().getItemStackFromSlot(FEET).getItem() instanceof SuitItem
                || event.getPlayer().getItemStackFromSlot(LEGS).getItem() instanceof SuitItem) {
            event.getPlayerModel().bipedRightLegwear.showModel = false;
            event.getPlayerModel().bipedLeftLegwear.showModel = false;
        }
    }
}
