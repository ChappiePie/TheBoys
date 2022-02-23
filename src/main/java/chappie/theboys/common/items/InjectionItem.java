package chappie.theboys.common.items;

import chappie.theboys.common.capability.BoysCap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
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
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
        playerIn.startUsingItem(hand);
        return InteractionResultHolder.consume(playerIn.getItemInHand(hand));
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int timeLeft) {
        if (pLivingEntity instanceof Player player) {
            player.getCapability(BoysCap.CAPABILITY).ifPresent(a -> {
                String injection = InjectionItem.getInjection(pStack);
                ItemStack offHandStack = pStack == player.getOffhandItem() ? player.getMainHandItem() : player.getOffhandItem();
                if (StringUtil.isNullOrEmpty(injection)) {
                    if (offHandStack.getItem() instanceof VialItem && !StringUtil.isNullOrEmpty(InjectionItem.getInjection(offHandStack))) {
                        InjectionItem.setInjection(pStack, InjectionItem.getInjection(offHandStack));
                        offHandStack.getOrCreateTag().remove("Injection");
                        return;
                    }

                    if (a.haveCompoundV()) {
                        InjectionItem.setInjection(pStack, "compound_v");
                        a.setCompoundV(false);
                        return;
                    }

                    if (HUPackSuperpowers.hasSuperpowers(player)) {
                        InjectionItem.setInjection(pStack, HUPackSuperpowers.getSuperpower(player).toString());
                        HUPackSuperpowers.removeSuperpower(player);
                    }
                } else {
                    if (offHandStack.getItem() instanceof VialItem && StringUtil.isNullOrEmpty(InjectionItem.getInjection(offHandStack))) {
                        InjectionItem.setInjection(offHandStack, InjectionItem.getInjection(pStack));
                        pStack.getOrCreateTag().remove("Injection");
                        return;
                    }

                    if (injection.equals("compound_v") && !a.haveCompoundV()) {
                        a.setCompoundV(true);
                        pStack.getOrCreateTag().remove("Injection");
                    }

                    if (!injection.equals("compound_v") && !HUPackSuperpowers.hasSuperpowers(player)) {
                        HUPackSuperpowers.setSuperpower(player, HUPackSuperpowers.getSuperpower(new ResourceLocation(injection)));
                        pStack.getOrCreateTag().remove("Injection");
                    }
                }
            });
        }
    }

    public static int getColor(ItemStack stack, int color) {
        var injection = stack.getOrCreateTag().getString("Injection");
        int id = injection.hashCode();
        if (color > 0 || StringUtil.isNullOrEmpty(injection)) {
            return 16777215;
        }

        if (injection.equals("compound_v")) {
            return 6009838;
        }
        var superpower = HUPackSuperpowers.getSuperpowers().get(ResourceLocation.tryParse(injection));
        if (superpower != null && superpower.jsonObject.has("color")) {
            var array = superpower.jsonObject.getAsJsonArray("color");
            var red = array.get(0).getAsInt();
            var green = array.get(1).getAsInt();
            var blue = array.get(2).getAsInt();
            return (red << 16) + (green << 8) + blue;
        }
        return id;
    }

    public static String getInjection(ItemStack stack) {
        return stack.getOrCreateTag().getString("Injection");
    }

    public static ItemStack setInjection(ItemStack stack, String injection) {
        stack.getOrCreateTag().putString("Injection", injection);
        return stack;
    }
}