package chappie.theboys.common.block.entity;

import chappie.theboys.TheBoys;
import chappie.theboys.common.block.TBBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class TBBlockEntities {

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType.BlockEntitySupplier<T> block) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, TheBoys.id(name), BlockEntityType.Builder.of(block, TBBlocks.SYNTHESIZER).build(null));
    }    public static final BlockEntityType<SynthesizerBlockEntity> SYNTHESIZER = register("synthesizer", SynthesizerBlockEntity::new);

    public static void init() {

    }


}
