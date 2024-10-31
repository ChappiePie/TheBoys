package chappie.theboys.common.item;

import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.common.capability.PowerCap;
import chappie.theboys.TheBoys;
import chappie.theboys.client.renderer.SyringeRenderer;
import chappie.theboys.common.capability.TheBoysCap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

    private String vialSuperpower(ItemStack pStack) {
        if (pStack.getTag() != null && pStack.getTag().contains("vial")) {
            return pStack.getTag().getCompound("vial").getCompound("tag").getString("superpower");
        }
        return "";
    }

    private boolean hasSuperpower(ItemStack pStack) {
        return pStack.getTag() != null && pStack.getTag().contains("vial") && pStack.getTag().getCompound("vial").getCompound("tag").contains("superpower");
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (pLivingEntity instanceof Player player && !pLevel.isClientSide()) {
            PowerCap cap = PowerCap.getCap(player);

            if (cap != null && pStack.hasTag()) {
                player.getCooldowns().addCooldown(this, 20);
                CompoundTag vialTag = pStack.getOrCreateTag().getCompound("vial");
                if (this.hasSuperpower(pStack)) {
                    if (this.vialSuperpower(pStack).equals("compoundV")) {
                        var superpowers = Superpower.REGISTRY.stream().filter(p -> Superpower.REGISTRY.getKey(p).getNamespace().equals(TheBoys.MODID)).toList();
                        cap.setSuperpower(superpowers.get(player.getRandom().nextInt(superpowers.size())));
                    } else if (cap.getSuperpower() == null) {
                        cap.setSuperpower(Superpower.REGISTRY.get(new ResourceLocation(vialTag.getCompound("tag").getString("superpower"))));
                        vialTag.getCompound("tag").remove("superpower");
                    } else {
                        if (!vialTag.contains("tag")) {
                            vialTag.put("tag", new CompoundTag());
                        }
                        vialTag.getCompound("tag").putString("superpower", Superpower.REGISTRY.getKey(cap.getSuperpower()).toString());
                        cap.setSuperpower(null);
                    }
                } else {
                    if (!vialTag.contains("tag")) {
                        vialTag.put("tag", new CompoundTag());
                    }
                    vialTag.getCompound("tag").putString("superpower", Superpower.REGISTRY.getKey(cap.getSuperpower()).toString());
                    cap.setSuperpower(null);
                }
                if (player.getRandom().nextBoolean()) {
                    var effects = BuiltInRegistries.MOB_EFFECT.stream().filter(p -> BuiltInRegistries.MOB_EFFECT.getKey(p).getNamespace().equals("minecraft") && p.getCategory().equals(MobEffectCategory.HARMFUL)).toList();
                    var mobEffect = effects.get(player.getRandom().nextInt(effects.size()));
                    player.addEffect(new MobEffectInstance(mobEffect, 200, 3, false, true, true));
                }
                if (!player.getAbilities().instabuild) {
                    vialTag.remove("tag");
                }
            }
        }
        return super.finishUsingItem(pStack, pLevel, pLivingEntity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack mainHandItem = pPlayer.getMainHandItem();
        ItemStack offHandItem = pPlayer.getOffhandItem();
        PowerCap cap = PowerCap.getCap(pPlayer);
        TheBoysCap boysCap = TheBoysCap.getCap(pPlayer);
        if (boysCap != null && cap != null && pHand == InteractionHand.MAIN_HAND) {
            if (mainHandItem.getTag() != null && mainHandItem.getTag().contains("vial")) {
                if (offHandItem.isEmpty()) {
                    if (pPlayer.isCrouching()) {
                        boysCap.vialAnim.triggerAnim(true, true);
                        return InteractionResultHolder.pass(pPlayer.getItemInHand(pHand));
                    }

                    if (boysCap.vialAnim.timeline.value(1) == 0) {
                        if (this.hasSuperpower(mainHandItem) || cap.getSuperpower() != null) {
                            boysCap.syringeAnim.triggerAnim(true);
                            return ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand);
                        } else {
                            pPlayer.displayClientMessage(Component.translatable("item.theboys.syringe.compoundV").withStyle(ChatFormatting.RED), true);
                        }
                    }
                } else {
                    pPlayer.displayClientMessage(Component.translatable("item.theboys.syringe.offHandSlot").withStyle(ChatFormatting.RED), true);
                }
            } else {
                if (offHandItem.getItem() instanceof VialItem) {
                    boysCap.vialAnim.triggerAnim(true, false);
                } else {
                    pPlayer.displayClientMessage(Component.translatable("item.theboys.syringe.noVial").withStyle(ChatFormatting.RED), true);
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
