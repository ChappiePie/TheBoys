package chappie.theboys.common.block;

import chappie.theboys.TheBoys;
import chappie.theboys.common.item.TBItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class TBBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, TheBoys.MODID);

    public static final DeferredHolder<Block, SynthesizerBlock> SYNTHESIZER = register("synthesizer",
            () -> new SynthesizerBlock(BlockBehaviour.Properties.of()));

    private static <T extends Block> DeferredHolder<Block, T> register(String id, Supplier<T> block) {
        DeferredHolder<Block, T> registered = BLOCKS.register(id, block);
        TBItems.register(id, () -> new BlockItem(registered.value(), new Item.Properties()));
        return registered;
    }

    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
