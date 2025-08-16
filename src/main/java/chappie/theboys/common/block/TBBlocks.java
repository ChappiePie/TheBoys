package chappie.theboys.common.block;

import chappie.theboys.TheBoys;
import chappie.theboys.common.item.TBItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public class TBBlocks {

    public static final SynthesizerBlock SYNTHESIZER = register("synthesizer", SynthesizerBlock::new, BlockBehaviour.Properties.of());

    private static <T extends Block> T register(String id, Function<BlockBehaviour.Properties, T> block, BlockBehaviour.Properties properties) {
        T b = Registry.register(BuiltInRegistries.BLOCK, TheBoys.id(id), block.apply(properties.setId(ResourceKey.create(Registries.BLOCK, TheBoys.id(id)))));
        TBItems.register(id, (p) -> new BlockItem(b, p), new Item.Properties());
        return b;
    }

    public static void init() {

    }
}
