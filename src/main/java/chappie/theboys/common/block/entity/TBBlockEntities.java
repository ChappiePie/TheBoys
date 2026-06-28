package chappie.theboys.common.block.entity;

import chappie.theboys.TheBoys;
import chappie.theboys.common.block.TBBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TBBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, TheBoys.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SynthesizerBlockEntity>> SYNTHESIZER = BLOCK_ENTITY_TYPES.register("synthesizer",
            () -> BlockEntityType.Builder.of(SynthesizerBlockEntity::new, TBBlocks.SYNTHESIZER.value()).build(null));

    public static void init(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
