package chappie.theboys.common.items;

import chappie.theboys.common.capability.BoysCap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;

import javax.annotation.Nullable;
import java.util.List;

public class VialItem extends Item {
    public VialItem(Properties propertiesIn) {
        super(propertiesIn);
    }

    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if(getCompoundV(stack)) tooltip.add(new TranslationTextComponent("Have Compound V"));
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity playerIn) {
        return stack;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
        playerIn.setActiveHand(hand);
        return new ActionResult(ActionResultType.SUCCESS, playerIn.getHeldItem(hand));
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity livingEntity, int timeLeft) {
        if (livingEntity instanceof PlayerEntity && timeLeft <= 71980) {
            PlayerEntity player = (PlayerEntity) livingEntity;
            player.getCapability(BoysCap.CAPABILITY).ifPresent(a -> {
                if (!getCompoundV(stack)) {
                    if (player.getHeldItemOffhand().getItem() == Items.WATER_BUCKET) {
                        setCompoundV(stack, true);
                        player.getHeldItemOffhand().shrink(1);
                    } else if (a.haveCompoundV()) {
                        setCompoundV(stack, true);
                        a.setCompoundV(false);
                    }
                } else if (!a.haveCompoundV() && getCompoundV(stack)) {
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

    public static void setCompoundV(ItemStack stack, boolean compound_v) {
        CompoundNBT nbt = stack.getOrCreateTag();
        nbt.putBoolean("compound_v", compound_v);
    }
}