package chappie.theboys.common.items;

import chappie.theboys.common.capability.BoysCap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

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
                if (!getCompoundV(stack) && a.haveCompoundV()) {
                    setCompoundV(stack, true);
                    a.setCompoundV(false);
                } else if (getCompoundV(stack) && !a.haveCompoundV()) {
                    a.setCompoundV(true);
                    setCompoundV(stack, false);
                }
            });
        }
    }

    public static boolean getCompoundV(ItemStack stack) {
        CompoundNBT nbt = stack.getTag();
        return nbt != null && nbt.getBoolean("compound_v");
    }

    public static ItemStack setCompoundV(ItemStack stack, boolean compound_v) {
        CompoundNBT nbt = stack.getOrCreateTag();
        nbt.putBoolean("compound_v", compound_v);
        return stack;
    }
}