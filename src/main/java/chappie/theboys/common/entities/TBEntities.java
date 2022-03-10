package chappie.theboys.common.entities;

import chappie.theboys.TheBoys;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TBEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, TheBoys.MODID);

    public static final RegistryObject<EntityType<TrailEntity>> TRAIL = register("trail", EntityType.Builder.<TrailEntity>of(TrailEntity::new, MobCategory.MISC).sized(1F, 1F));
    public static final RegistryObject<EntityType<LightningProjectile>> LIGHTNING_PROJECTILE = register("lightning_projectile", EntityType.Builder.<LightningProjectile>of(LightningProjectile::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true));

    private static <E extends Entity> RegistryObject<EntityType<E>> register(String name, EntityType.Builder<E> builder) {
        return ENTITIES.register(name, () -> builder.build(TheBoys.MODID + ":" + name));
    }
}
