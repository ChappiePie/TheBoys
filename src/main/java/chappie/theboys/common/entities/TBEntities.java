package chappie.theboys.common.entities;

import chappie.theboys.TheBoys;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TBEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, TheBoys.MODID);

    public static final EntityType<TrailEntity> TRAIL = register("trail", EntityType.Builder.<TrailEntity>of(TrailEntity::new, MobCategory.MISC).sized(1F, 1F).build(TheBoys.MODID + ":trail"));
    public static final EntityType<LightningProjectile> LIGHTNING_PROJECTILE = register("lightning_projectile", EntityType.Builder.<LightningProjectile>of(LightningProjectile::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true).build(TheBoys.MODID + ":lightning_projectile"));

    private static <E extends Entity, T extends EntityType<E>> T register(String name, T entity) {
        ENTITIES.register(name, () -> entity);
        return entity;
    }
}
