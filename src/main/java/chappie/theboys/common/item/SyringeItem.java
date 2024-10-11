package chappie.theboys.common.item;

import chappie.modulus.common.capability.anim.PlayerAnimCap;
import chappie.theboys.client.renderer.SyringeRenderer;
import chappie.theboys.common.capability.TheBoysCap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SyringeItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

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
            var effects = BuiltInRegistries.MOB_EFFECT.stream().filter(p -> BuiltInRegistries.MOB_EFFECT.getKey(p).getNamespace().equals("minecraft") && p.getCategory().equals(MobEffectCategory.HARMFUL)).toList();
            var mobEffect = effects.get(player.getRandom().nextInt(effects.size()));
            if (player.getRandom().nextBoolean()) {
                player.addEffect(new MobEffectInstance(mobEffect, 200, 3, false, true, true));
            }
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
            if (mainHandItem.getTag() != null && mainHandItem.getTag().contains("vial")) {
                if (offHandItem.isEmpty()) {
                    if (boysCap.vialAnim.timeline.value(1) == 0) {
                        boysCap.syringeAnim.triggerAnim = true;
                        boysCap.syncToAll();
                        return ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand);
                    }
                } else {
                    pPlayer.displayClientMessage(Component.literal("Remove the item from off hand slot").withStyle(ChatFormatting.RED), true);
                }
            } else {
                if (offHandItem.getItem() instanceof VialItem) {
                    boysCap.vialAnim.triggerAnim = true;
                    boysCap.syncToAll();
                } else {
                    pPlayer.displayClientMessage(Component.literal("Place vial in off hand slot").withStyle(ChatFormatting.RED), true);
                }
            }
        }

        return InteractionResultHolder.pass(pPlayer.getItemInHand(pHand));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
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
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }
}
