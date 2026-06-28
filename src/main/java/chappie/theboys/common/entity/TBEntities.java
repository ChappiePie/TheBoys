package chappie.theboys.common.entity;

import chappie.theboys.TheBoys;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TBEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, TheBoys.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<TrailEntity>> TRAIL = ENTITY_TYPES.register("trail",
            () -> EntityType.Builder.<TrailEntity>of(TrailEntity::new, MobCategory.MISC).sized(1F, 1F).build(TheBoys.id("trail").toString()));

    public static void init(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
