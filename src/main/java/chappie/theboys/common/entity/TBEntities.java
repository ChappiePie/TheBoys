package chappie.theboys.common.entity;

import chappie.theboys.TheBoys;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class TBEntities {

    public static final EntityType<TrailEntity> TRAIL = register("trail", EntityType.Builder.<TrailEntity>of(TrailEntity::new, MobCategory.MISC).sized(1F, 1F));

    private static <E extends Entity> EntityType<E> register(String name, EntityType.Builder<E> builder) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, TheBoys.id(name), builder.build(TheBoys.MODID + ":" + name));
    }

    public static void init() {

    }
}
