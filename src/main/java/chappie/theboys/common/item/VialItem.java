package chappie.theboys.common.item;

import chappie.theboys.client.renderer.VialRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class VialItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public VialItem() {
        super(new Properties().stacksTo(64));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public static int getColor(CompoundTag tag) {
        if (tag != null) {
            if (tag.contains("color", 99)) {
                return tag.getInt("color");
            }
            if (tag.getBoolean("compoundV")) {
                return 104166;
            }
        }
        return -1;
    }

    public static ItemStack compoundV() {
        ItemStack pStack = TBItems.VIAL.getDefaultInstance();
        pStack.getOrCreateTag().putBoolean("compoundV", true);
        return pStack;
    }

    public boolean hasCustomColor(ItemStack pStack) {
        CompoundTag compoundtag = pStack.getTag();
        return compoundtag != null && compoundtag.contains("color", 99);
    }

    public void clearColor(ItemStack pStack) {
        CompoundTag compoundtag = pStack.getTag();
        if (compoundtag != null && compoundtag.contains("color")) {
            compoundtag.remove("color");
        }

    }

    public void setColor(ItemStack pStack, int pColor) {
        pStack.getOrCreateTag().putInt("color", pColor);
    }

    public int getColor(ItemStack pStack) {
        return VialItem.getColor(pStack.getTag());
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().contains("compoundV") ? "injection.theboys.compound_v" : super.getDescriptionId(stack);
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
            private VialRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new VialRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }
}
