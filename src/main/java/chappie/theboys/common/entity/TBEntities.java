package chappie.theboys.common.entity;

import chappie.theboys.TheBoys;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TBEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TheBoys.MODID);

    public static final RegistryObject<EntityType<TrailEntity>> TRAIL = register("trail", EntityType.Builder.<TrailEntity>of(TrailEntity::new, MobCategory.MISC).sized(1F, 1F));

    private static <E extends Entity> RegistryObject<EntityType<E>> register(String name, EntityType.Builder<E> builder) {
        return ENTITIES.register(name, () -> builder.build(TheBoys.MODID + ":" + name));
    }
}
