package chappie.theboys.common.items;

import chappie.theboys.client.render.ScrapRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.ForgeTier;
import software.bernie.HU.geckolib3.core.IAnimatable;
import software.bernie.HU.geckolib3.core.manager.AnimationData;
import software.bernie.HU.geckolib3.core.manager.AnimationFactory;

import java.util.function.Consumer;

public class ScrapItem extends DiggerItem implements IAnimatable {

    public static final ForgeTier IRON_SCRAP = new ForgeTier(2, 350, 8.0F, 3.0F, 12, BlockTags.MINEABLE_WITH_PICKAXE, () -> Ingredient.of(Items.IRON_INGOT));

    public ScrapItem(float attackDamageBaseline, float attackSpeed, Item.Properties properties) {
        super(attackDamageBaseline, attackSpeed, IRON_SCRAP, BlockTags.MINEABLE_WITH_PICKAXE, properties.tab(CreativeModeTab.TAB_TOOLS).stacksTo(1));
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IItemRenderProperties() {
            private final BlockEntityWithoutLevelRenderer renderer = new ScrapRenderer();
            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return this.renderer;
            }
        });
    }

    public float getDestroySpeed(ItemStack stack, BlockState state) {
        Material material = state.getMaterial();
        return material != Material.METAL && material != Material.HEAVY_METAL && material != Material.STONE ? super.getDestroySpeed(stack, state) : this.speed;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
    }

    @Override
    public AnimationFactory getFactory() {
        return new AnimationFactory(this);
    }
}