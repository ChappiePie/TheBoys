package chappie.theboys.common.item;

import chappie.modulus.common.ability.base.Superpower;
import chappie.theboys.client.renderer.VialRenderer;
import chappie.theboys.common.ability.base.TBSuperpower;
import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.util.tooltip.SuperpowerTooltip;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class VialItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public VialItem(Properties properties) {
        super(properties.stacksTo(64));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public static int getColor(ItemStack stack) {
        String superpower = stack.getOrDefault(TBDataComponents.SUPERPOWER, "");
        if (!superpower.isEmpty()) {
            if (superpower.equals("compoundV")) {
                return -14117156;
            }
            return superpower.hashCode();
        }
        return -1;
    }

    public static ItemStack compoundV() {
        ItemStack pStack = TBItems.VIAL.get().getDefaultInstance();
        pStack.set(TBDataComponents.SUPERPOWER, "compoundV");
        return pStack;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Objects.equals(stack.getOrDefault(TBDataComponents.SUPERPOWER, ""), "compoundV") ? Component.translatable("injection.theboys.compound_v") : super.getName(stack);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        String superpower = stack.getOrDefault(TBDataComponents.SUPERPOWER, "");
        if (!superpower.isEmpty() && !superpower.equals("compoundV")) {
            Superpower sup = Superpower.REGISTRY.get(ResourceLocation.tryParse(superpower));
            if (sup instanceof TBSuperpower tbs) {
                return Optional.of(new SuperpowerTooltip(tbs));
            }
        }
        return Optional.empty();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        GeoItem.super.createGeoRenderer(consumer);
        consumer.accept(new GeoRenderProvider() {
            private VialRenderer renderer;

            @Override
            public @Nullable GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new VialRenderer();

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
                    this.renderer = new VialRenderer();
                }
                return this.renderer;
            }
        });
    }
}
