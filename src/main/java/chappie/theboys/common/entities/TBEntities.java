package chappie.theboys.common.entities;

import chappie.theboys.TheBoys;
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

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> e) {
        e.getRegistry().register(EntityType.Builder.<TrailEntity>create(TrailEntity::new, EntityClassification.MISC).size(1F, 1F).setCustomClientFactory((spawnEntity, world) -> TRAIL.create(world)).build(TheBoys.MODID + ":trail").setRegistryName("trail"));
    }

    public static void EntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(TRAIL, TrailRenderer::new);
    }
}
