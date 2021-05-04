package chappie.theboys.common.entities;

import chappie.theboys.TheBoys;
import chappie.theboys.client.render.LightningProjectileRenderer;
import chappie.theboys.client.render.TrailRenderer;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(TheBoys.MODID)
@Mod.EventBusSubscriber(modid = TheBoys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TBEntities {


    @ObjectHolder("trail")
    public static final EntityType<TrailEntity> TRAIL = null;
    public static final EntityType<LightningProjectile> LIGHTNING_PROJECTILE = null;

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> e) {
        e.getRegistry().register(EntityType.Builder.<TrailEntity>of(TrailEntity::new, EntityClassification.MISC).sized(1F, 1F).setCustomClientFactory((spawnEntity, world) -> TRAIL.create(world)).build(TheBoys.MODID + ":trail").setRegistryName("trail"));
        e.getRegistry().register(EntityType.Builder.<LightningProjectile>of(LightningProjectile::new, EntityClassification.MISC).sized(0.5F, 0.5F).setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true).build(TheBoys.MODID + ":lightning_projectile").setRegistryName("lightning_projectile"));
    }

    public static void EntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(TRAIL, TrailRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(LIGHTNING_PROJECTILE, LightningProjectileRenderer::new);
    }
}
