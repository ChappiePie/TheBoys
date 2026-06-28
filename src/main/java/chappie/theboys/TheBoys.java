package chappie.theboys;

import chappie.theboys.common.ability.base.TBAbilityTypes;
import chappie.theboys.common.ability.base.TBSuperpowers;
import chappie.theboys.common.block.TBBlocks;
import chappie.theboys.common.block.entity.TBBlockEntities;
import chappie.theboys.common.block.menu.TBMenus;
import chappie.theboys.common.capability.TBAttachments;
import chappie.theboys.common.capability.TBEntityCap;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.entity.TBEntities;
import chappie.theboys.common.entity.TrailEntity;
import chappie.theboys.common.item.TBItems;
import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.common.particle.TBParticleTypes;
import chappie.theboys.networking.TBNetworking;
import chappie.theboys.util.TBConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(TheBoys.MODID)
public class TheBoys {
    public static final String MODID = "theboys";

    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static ResourceLocation id(String id) {
        return ResourceLocation.fromNamespaceAndPath(MODID, id);
    }

    public TheBoys(IEventBus modEventBus, net.neoforged.fml.ModContainer container) {
        TBDataComponents.init(modEventBus);
        TBItems.init(modEventBus);
        TBAbilityTypes.init();
        TBSuperpowers.init();
        TBEntities.init(modEventBus);
        TBParticleTypes.init(modEventBus);

        TBBlocks.init(modEventBus);
        TBBlockEntities.init(modEventBus);
        TBMenus.init(modEventBus);
        TBAttachments.register(modEventBus);

        container.registerConfig(ModConfig.Type.CLIENT, TBConfig.CLIENT_SPEC);
        container.registerConfig(ModConfig.Type.COMMON, TBConfig.COMMON_SPEC);

        TBNetworking.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(this::onLivingTick);
        NeoForge.EVENT_BUS.addListener(this::onStartTracking);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(this::onPlayerRespawn);
    }

    private void onLivingTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            TheBoysCap cap = TheBoysCap.getCap(entity);
            if (cap != null) {
                cap.tick();
            }

            TBEntityCap entityCap = TBEntityCap.getCap(entity);
            if (entityCap != null) {
                entityCap.tick();
            }
        }
    }

    private void onStartTracking(PlayerEvent.StartTracking event) {
        Entity target = event.getTarget();

        if (target instanceof TrailEntity trailEntity) {
            if (event.getEntity() instanceof ServerPlayer player) {
                TrailEntity.startTracking(trailEntity, player);
            }
        }
    }

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TheBoysCap cap = TheBoysCap.getCap(player);
            if (cap != null) {
                cap.syncToAll();
            }
        }
    }

    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TheBoysCap cap = TheBoysCap.getCap(player);
            if (cap != null) {
                cap.syncToAll();
            }
        }
    }
}
