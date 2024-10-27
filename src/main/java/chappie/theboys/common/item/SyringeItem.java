package chappie.theboys.common.item;

import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.common.capability.anim.PlayerAnimCap;
import chappie.theboys.TheBoys;
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

    public int getColor(ItemStack pStack) {
        CompoundTag compoundtag = pStack.getTag();
        return compoundtag != null ? VialItem.getColor(compoundtag.getCompound("vial").getCompound("tag")) : -1;
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 30;
    }

    private boolean hasCompoundV(ItemStack pStack) {
        return pStack.getTag() != null && pStack.getTag().contains("vial") && pStack.getTag().getCompound("vial").getCompound("tag").getBoolean("compoundV");
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (pLivingEntity instanceof Player player && !pLevel.isClientSide() && this.hasCompoundV(pStack)) {
            player.getCooldowns().addCooldown(this, 20);
            var superpowers = Superpower.REGISTRY.stream().filter(p -> Superpower.REGISTRY.getKey(p).getNamespace().equals(TheBoys.MODID)).toList();
            var power = superpowers.get(player.getRandom().nextInt(superpowers.size()));

            var effects = BuiltInRegistries.MOB_EFFECT.stream().filter(p -> BuiltInRegistries.MOB_EFFECT.getKey(p).getNamespace().equals("minecraft") && p.getCategory().equals(MobEffectCategory.HARMFUL)).toList();
            var mobEffect = effects.get(player.getRandom().nextInt(effects.size()));
            PowerCap cap = PowerCap.getCap(player);
            if (cap != null) {
                cap.setSuperpower(power);
            }
            if (player.getRandom().nextBoolean()) {
                player.addEffect(new MobEffectInstance(mobEffect, 200, 3, false, true, true));
            }
            if (!player.getAbilities().instabuild) {
                pStack.getOrCreateTag().getCompound("vial").remove("tag");
            }
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
                    if (pPlayer.isCrouching()) {
                        boysCap.vialAnim.reverse = true;
                        boysCap.vialAnim.triggerAnim = true;
                        boysCap.syncToAll();
                        return InteractionResultHolder.pass(pPlayer.getItemInHand(pHand));
                    }

                    if (boysCap.vialAnim.timeline.value(1) == 0) {
                        if (this.hasCompoundV(mainHandItem)) {
                            boysCap.syringeAnim.triggerAnim = true;
                            boysCap.syncToAll();
                            return ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand);
                        } else {
                            pPlayer.displayClientMessage(Component.literal("There is no compound V").withStyle(ChatFormatting.RED), true);
                        }
                    }
                } else {
                    pPlayer.displayClientMessage(Component.literal("Remove the item from off hand slot").withStyle(ChatFormatting.RED), true);
                }
            } else {
                if (offHandItem.getItem() instanceof VialItem) {
                    boysCap.vialAnim.reverse = false;
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
