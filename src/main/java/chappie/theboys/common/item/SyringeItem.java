package chappie.theboys.common.item;

import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.common.capability.PowerCap;
import chappie.theboys.TheBoys;
import chappie.theboys.client.renderer.SyringeRenderer;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.util.TBConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class SyringeItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SyringeItem(Properties properties) {
        super(properties.stacksTo(1));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public static int getColor(ItemStack pStack) {
        ItemStack vial = pStack.getOrDefault(TBDataComponents.VIAL, ItemStack.EMPTY);
        return !vial.isEmpty() ? VialItem.getColor(vial) : -1;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 30;
    }

    private String vialSuperpower(ItemStack pStack) {
        ItemStack vial = pStack.getOrDefault(TBDataComponents.VIAL, ItemStack.EMPTY);
        if (!vial.isEmpty()) {
            return vial.getOrDefault(TBDataComponents.SUPERPOWER, "");
        }
        return "";
    }

    private boolean hasSuperpower(ItemStack pStack) {
        ItemStack vial = pStack.getOrDefault(TBDataComponents.VIAL, ItemStack.EMPTY);
        return !vial.isEmpty() && !vial.getOrDefault(TBDataComponents.SUPERPOWER, "").isEmpty();
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (pLivingEntity instanceof Player player) {
            PowerCap cap = PowerCap.getCap(player);
            boolean b = false;
            ItemStack vial = pStack.getOrDefault(TBDataComponents.VIAL, ItemStack.EMPTY);
            if (cap != null && !vial.isEmpty()) {
                player.getCooldowns().addCooldown(pStack, 20);
                if (this.hasSuperpower(pStack) && cap.getSuperpower() == null) {
                    if (this.vialSuperpower(pStack).equals("compoundV")) {
                        var superpowers = Superpower.REGISTRY.stream().filter(p -> Superpower.REGISTRY.getKey(p).getNamespace().equals(TheBoys.MODID)).toList();
                        cap.setSuperpower(superpowers.get(player.getRandom().nextInt(superpowers.size())));
                    } else {
                        cap.setSuperpower(Superpower.REGISTRY.get(ResourceLocation.tryParse(vial.getOrDefault(TBDataComponents.SUPERPOWER, ""))).get().value());
                    }
                    if (!player.getAbilities().instabuild) {
                        vial.remove(TBDataComponents.SUPERPOWER);
                        pStack.set(TBDataComponents.VIAL, vial);
                        this.broadcastChangesOnContainerMenu(player);
                        b = true;
                    }
                } else {
                    if (vial.getOrDefault(TBDataComponents.SUPERPOWER, "").isEmpty()) {
                        if (TBConfig.COMMON.storeAbilities.get() || player.getAbilities().instabuild) {
                            vial.set(TBDataComponents.SUPERPOWER, Superpower.REGISTRY.getKey(cap.getSuperpower()).toString());
                        } else {
                            vial.set(TBDataComponents.SUPERPOWER, "compoundV");
                        }
                        this.broadcastChangesOnContainerMenu(player);
                        cap.setSuperpower(null);
                        b = true;
                    } else {
                        if (this.vialSuperpower(pStack).equals("compoundV")) {
                            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 3, false, true, true));
                            if (!player.getAbilities().instabuild) {
                                vial.remove(TBDataComponents.SUPERPOWER);
                            }
                        }
                    }
                    pStack.set(TBDataComponents.VIAL, vial);
                    this.broadcastChangesOnContainerMenu(player);
                }
                if (player.getRandom().nextBoolean() && b) {
                    var effects = BuiltInRegistries.MOB_EFFECT.stream().filter(p -> BuiltInRegistries.MOB_EFFECT.getKey(p).getNamespace().equals("minecraft") && p.getCategory().equals(MobEffectCategory.HARMFUL)).toList();
                    var mobEffect = effects.get(player.getRandom().nextInt(effects.size()));
                    player.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(mobEffect), 200, 3, false, true, true));
                }
            }
        }
        return super.finishUsingItem(pStack, pLevel, pLivingEntity);
    }

    private void broadcastChangesOnContainerMenu(Player player) {
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        if (abstractContainerMenu != null) {
            abstractContainerMenu.slotsChanged(player.getInventory());
        }
    }

    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack mainHandItem = pPlayer.getMainHandItem();
        ItemStack offHandItem = pPlayer.getOffhandItem();
        PowerCap cap = PowerCap.getCap(pPlayer);
        TheBoysCap boysCap = TheBoysCap.getCap(pPlayer);
        if (boysCap != null && cap != null && pHand == InteractionHand.MAIN_HAND) {
            ItemStack vial = mainHandItem.getOrDefault(TBDataComponents.VIAL, ItemStack.EMPTY);
            if (!vial.isEmpty()) {
                if (offHandItem.isEmpty()) {
                    if (pPlayer.isCrouching()) {
                        boysCap.vialAnim.triggerAnim(true, true);
                        return InteractionResult.PASS;
                    }

                    if (boysCap.vialAnim.timeline.value(1) == 0) {
                        boolean use = false;
                        if (this.hasSuperpower(mainHandItem)) {
                            if (cap.getSuperpower() == null) {
                                use = true;
                            } else {
                                if (vialSuperpower(mainHandItem).equals("compoundV")) {
                                    use = true;
                                } else {
                                    if (pPlayer.getAbilities().instabuild) {
                                        use = true;
                                    } else {
                                        pPlayer.displayClientMessage(Component.translatable("item.theboys.syringe.compoundV").withStyle(ChatFormatting.RED), true);
                                    }
                                }
                            }
                        } else {
                            if (cap.getSuperpower() != null) {
                                use = true;
                            }
                        }
                        if (use) {
                            boysCap.syringeAnim.triggerAnim(true);
                            return ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand);
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

        return InteractionResult.PASS;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        GeoItem.super.createGeoRenderer(consumer);
        consumer.accept(new GeoRenderProvider() {
            private SyringeRenderer renderer;

            @Override
            public @Nullable GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new SyringeRenderer();

                return this.renderer;
            }
        });
    }
}
