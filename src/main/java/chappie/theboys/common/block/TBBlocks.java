package chappie.theboys.common.block;

import chappie.theboys.TheBoys;
import chappie.theboys.common.item.TBItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TBBlocks {

    public static final SynthesizerBlock SYNTHESIZER = register("synthesizer", new SynthesizerBlock(BlockBehaviour.Properties.of()));

    private static <T extends Block> T register(String name, T block) {
        T b = Registry.register(BuiltInRegistries.BLOCK, TheBoys.id(name), block);
        TBItems.register(name, new BlockItem(b, new Item.Properties()));
        return b;
    }

    public static void init() {

    }
}
