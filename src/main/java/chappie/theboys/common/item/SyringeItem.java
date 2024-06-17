package chappie.theboys.common.item;

import chappie.modulus.common.capability.anim.PlayerAnimCap;
import chappie.theboys.TheBoys;
import chappie.theboys.client.renderer.SyringeRenderer;
import chappie.theboys.common.capability.TheBoysCap;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class SyringeItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SyringeItem() {
        super(new Item.Properties().stacksTo(1));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public boolean hasCustomColor(ItemStack pStack) {
        CompoundTag compoundtag = pStack.getTag();
        return compoundtag != null && compoundtag.getCompound("vial").contains("color", 99);
    }

    public int getColor(ItemStack pStack) {
        CompoundTag compoundtag = pStack.getTag();
        return compoundtag != null && compoundtag.getCompound("vial").getCompound("tag").contains("color", 99) ? compoundtag.getCompound("vial").getCompound("tag").getInt("color") : -1;
    }

    public void clearColor(ItemStack pStack) {
        CompoundTag compoundtag = pStack.getTag();
        if (compoundtag != null && compoundtag.contains("color")) {
            compoundtag.getCompound("vial").remove("color");
        }

    }

    public void setColor(ItemStack pStack, int pColor) {
        pStack.getOrCreateTag().getCompound("vial").putInt("color", pColor);
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 30;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (pLivingEntity instanceof Player player && !pLevel.isClientSide()) {
            player.getCooldowns().addCooldown(this, 20);
            var effects = ForgeRegistries.MOB_EFFECTS.getValues().stream().filter(p -> ForgeRegistries.MOB_EFFECTS.getKey(p).getNamespace().equals("minecraft") && p.getCategory().equals(MobEffectCategory.HARMFUL)).toList();
            var mobEffect = effects.get(player.getRandom().nextInt(effects.size()));
            player.addEffect(new MobEffectInstance(mobEffect, 200, 3, false, true, true));
            //mainHandItem.getOrCreateTag().put("vial",);
        }
        return super.finishUsingItem(pStack, pLevel, pLivingEntity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack mainHandItem = pPlayer.getMainHandItem();
        ItemStack offHandItem = pPlayer.getOffhandItem();
        PlayerAnimCap cap = PlayerAnimCap.getCap(pPlayer);
        TheBoysCap boysCap = TheBoysCap.getCap(pPlayer);
        if (boysCap != null && cap != null && pHand == InteractionHand.MAIN_HAND) {
            if (mainHandItem.getTag() != null && mainHandItem.getTag().contains("vial") && boysCap.vialAnim.timeline.value(1) == 0) {
                cap.triggerAnim("theboys_syringe_controller", true, "inject");
                cap.triggerAnim("theboys_syringe_controller", false, "inject" + (pPlayer.getMainArm() == HumanoidArm.LEFT ? "_left" : ""));
                return ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand);
            } else {
                if (offHandItem.getItem() instanceof VialItem) {
                    boysCap.vialAnim.triggerAnim = true;
                }
            }
        }

        return InteractionResultHolder.pass(pPlayer.getItemInHand(pHand));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private SyringeRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new SyringeRenderer();

                return this.renderer;
            }
        });
    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
