package chappie.theboys.common.item;

import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.common.capability.PowerCap;
import chappie.theboys.TheBoys;
import chappie.theboys.client.renderer.SyringeRenderer;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.common.item.datacomponents.VialContents;
import chappie.theboys.util.TBConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
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
        return pStack.has(TBDataComponents.VIAL) ? VialItem.getColor(pStack.getOrDefault(TBDataComponents.VIAL, VialContents.EMPTY).toStack()) : -1;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 30;
    }

    private String vialSuperpower(ItemStack pStack) {
        VialContents vial = pStack.getOrDefault(TBDataComponents.VIAL, VialContents.EMPTY);
        if (pStack.has(TBDataComponents.VIAL)) {
            return vial.superpower();
        }
        return "";
    }

    private boolean hasSuperpower(ItemStack pStack) {
        return pStack.has(TBDataComponents.VIAL) && pStack.getOrDefault(TBDataComponents.VIAL, VialContents.EMPTY).hasSuperpower();
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (pLivingEntity instanceof Player player) {
            PowerCap cap = PowerCap.getCap(player);
            boolean b = false;
            VialContents vial = pStack.getOrDefault(TBDataComponents.VIAL, VialContents.EMPTY);
            if (cap != null && pStack.has(TBDataComponents.VIAL)) {
                player.getCooldowns().addCooldown(pStack.getItem(), 20);
                if (this.hasSuperpower(pStack) && (cap.getSuperpower() == null || player.getAbilities().instabuild && !this.vialSuperpower(pStack).equals("compoundV"))) {
                    if (this.vialSuperpower(pStack).equals("compoundV")) {
                        if (cap.getSuperpower() == null) {
                            var superpowers = Superpower.REGISTRY.stream().filter(p -> Superpower.REGISTRY.getKey(p).getNamespace().equals(TheBoys.MODID)).toList();
                            cap.setSuperpower(superpowers.get(player.getRandom().nextInt(superpowers.size())));
                        }
                    } else {
                        String superpower = vial.superpower();
                        if (!superpower.isBlank()) {
                            cap.setSuperpower(Superpower.REGISTRY.get(ResourceLocation.tryParse(superpower)));
                        }
                    }

                    if (!player.getAbilities().instabuild) {
                        vial = VialContents.EMPTY;
                        pStack.set(TBDataComponents.VIAL, vial);
                        this.broadcastChangesOnContainerMenu(player);
                        b = true;
                    }
                } else {
                    if (!vial.hasSuperpower()) {
                        if (TBConfig.COMMON.storeAbilities.get() || player.getAbilities().instabuild) {
                            vial = new VialContents(Superpower.REGISTRY.getKey(cap.getSuperpower()).toString());
                        } else {
                            vial = new VialContents("compoundV");
                        }
                        this.broadcastChangesOnContainerMenu(player);
                        cap.setSuperpower(null);
                        b = true;
                    } else {
                        if (this.vialSuperpower(pStack).equals("compoundV")) {
                            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 3, false, true, true));
                            if (!player.getAbilities().instabuild) {
                                vial = VialContents.EMPTY;
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
        player.containerMenu.slotsChanged(player.getInventory());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack mainHandItem = pPlayer.getMainHandItem();
        ItemStack offHandItem = pPlayer.getOffhandItem();
        PowerCap cap = PowerCap.getCap(pPlayer);
        TheBoysCap boysCap = TheBoysCap.getCap(pPlayer);
        if (boysCap != null && cap != null && pHand == InteractionHand.MAIN_HAND) {
            if (mainHandItem.has(TBDataComponents.VIAL)) {
                if (offHandItem.isEmpty()) {
                    if (pPlayer.isCrouching()) {
                        boysCap.vialAnim.triggerAnim(true, true);
                        return InteractionResultHolder.pass(mainHandItem);
                    }

                    if (boysCap.vialAnim.timeline.value(1) == 0) {
                        boolean use = false;
                        if (this.hasSuperpower(mainHandItem)) {
                            if (cap.getSuperpower() == null) {
                                use = true;
                            } else {
                                if (vialSuperpower(mainHandItem).equals("compoundV") || pPlayer.getAbilities().instabuild) {
                                    use = true;
                                } else {
                                    pPlayer.displayClientMessage(Component.translatable("item.theboys.syringe.compoundV").withStyle(ChatFormatting.RED), true);
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

        return InteractionResultHolder.pass(mainHandItem);
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
            public @NotNull GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new SyringeRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoItemRenderer<?> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new SyringeRenderer();
                }
                return this.renderer;
            }
        });
    }
}
