package chappie.theboys.common.items;

import chappie.theboys.client.render.ScrapRenderer;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import software.bernie.HU.geckolib3.core.IAnimatable;
import software.bernie.HU.geckolib3.core.manager.AnimationData;
import software.bernie.HU.geckolib3.core.manager.AnimationFactory;
import xyz.heroesunited.heroesunited.util.HUItemTier;

import java.util.Set;

public class ScrapItem extends ToolItem implements IAnimatable {

    public static final HUItemTier IRON_SCRAP = new HUItemTier(2, 350, 8.0F, 3.0F, 12, () -> Ingredient.of(Items.IRON_INGOT));
    private static final Set<Block> DIGGABLES = ImmutableSet.of(Blocks.ACTIVATOR_RAIL, Blocks.DETECTOR_RAIL, Blocks.POWERED_RAIL, Blocks.RAIL, Blocks.STONE_BUTTON, Blocks.STONE_PRESSURE_PLATE, Blocks.PISTON, Blocks.STICKY_PISTON, Blocks.PISTON_HEAD);

    public ScrapItem(float attackDamageBaseline, float attackSpeed, Item.Properties properties) {
        super(attackDamageBaseline, attackSpeed, IRON_SCRAP, DIGGABLES, properties.tab(ItemGroup.TAB_TOOLS).stacksTo(1).setISTER(() -> ScrapRenderer::new));
    }

    public boolean isCorrectToolForDrops(BlockState state) {
        int i = this.getTier().getLevel();
        if (state.getHarvestTool() == net.minecraftforge.common.ToolType.PICKAXE) {
            return i >= state.getHarvestLevel();
        }
        Material material = state.getMaterial();
        return material == Material.STONE || material == Material.METAL || material == Material.HEAVY_METAL;
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