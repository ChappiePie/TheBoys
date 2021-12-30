package chappie.theboys.common.items;

import chappie.theboys.common.capability.BoysCap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import xyz.heroesunited.heroesunited.hupacks.HUPackSuperpowers;

public class InjectionItem extends Item {
    public InjectionItem(Properties propertiesIn) {
        super(propertiesIn);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 36000;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity playerIn) {
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
        playerIn.startUsingItem(hand);
        return new InteractionResultHolder(InteractionResult.SUCCESS, playerIn.getItemInHand(hand));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity livingEntity, int timeLeft) {
        if (livingEntity instanceof Player && timeLeft <= 35980) {
            Player player = (Player) livingEntity;
            player.getCapability(BoysCap.CAPABILITY).ifPresent(a -> {
                if (StringUtil.isNullOrEmpty(getInjection(stack))) {
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