package chappie.theboys.common.items;

import chappie.theboys.common.capability.BoysCap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.*;
import net.minecraft.world.World;
import xyz.heroesunited.heroesunited.hupacks.HUPackSuperpowers;

public class InjectionItem extends Item {
    public InjectionItem(Properties propertiesIn) {
        super(propertiesIn);
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 36000;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, World worldIn, LivingEntity playerIn) {
        return stack;
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand hand) {
        playerIn.startUsingItem(hand);
        return new ActionResult(ActionResultType.SUCCESS, playerIn.getItemInHand(hand));
    }

    @Override
    public void releaseUsing(ItemStack stack, World worldIn, LivingEntity livingEntity, int timeLeft) {
        if (livingEntity instanceof PlayerEntity && timeLeft <= 35980) {
            PlayerEntity player = (PlayerEntity) livingEntity;
            player.getCapability(BoysCap.CAPABILITY).ifPresent(a -> {
                if (StringUtils.isNullOrEmpty(getInjection(stack))) {
                    if (a.haveCompoundV()) {
                        setInjection(stack, "compound_v");
                        a.setCompoundV(false);
                    } else if (HUPackSuperpowers.hasSuperpowers(player)) {
                        setInjection(stack, HUPackSuperpowers.getSuperpower(player).toString());
                        HUPackSuperpowers.removeSuperpower(player);
                    }
                } else if (getInjection(stack).equals("compound_v") && !a.haveCompoundV()) {
                    a.setCompoundV(true);
                    setInjection(stack, "");
                }else if (!getInjection(stack).equals("compound_v") && !HUPackSuperpowers.hasSuperpowers(player)) {
                    if (!worldIn.isClientSide) {
                        HUPackSuperpowers.setSuperpower(player, HUPackSuperpowers.getSuperpower(new ResourceLocation(getInjection(stack))));
                    }
                    setInjection(stack, "");
                }
            });
        }
    }

    public static String getInjection(ItemStack stack) {
        return stack.getOrCreateTag().getString("Injection");
    }

    public static ItemStack setInjection(ItemStack stack, String injection) {
        stack.getOrCreateTag().putString("Injection", injection);
        return stack;
    }
}