package chappie.theboys;

import chappie.theboys.abilities.SpeedsterSuit;
import chappie.theboys.abilities.TBAbilityTypes;
import chappie.theboys.client.ATrainOverlay;
import chappie.theboys.client.TBClientEventHandler;
import chappie.theboys.client.render.LightningProjectileRenderer;
import chappie.theboys.client.render.TrailRenderer;
import chappie.theboys.common.TBEventHandler;
import chappie.theboys.common.capability.IBoys;
import chappie.theboys.common.capability.TBCapabilityEvents;
import chappie.theboys.common.entities.TBEntities;
import chappie.theboys.common.items.InjectionItem;
import chappie.theboys.common.items.TBItems;
import chappie.theboys.network.TBNetworking;
import chappie.theboys.util.TBRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.heroesunited.heroesunited.client.gui.AbilitiesScreen;
import xyz.heroesunited.heroesunited.hupacks.HUPackSuit;

@Mod(TheBoys.MODID)
public class TheBoys {
    public static final String MODID = "theboys";
    public static final Logger LOGGER = LogManager.getLogger();

    public TheBoys() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);

        TBAbilityTypes.ABILITIES.register(bus);
        TBItems.ITEMS.register(bus);
        TBEntities.ENTITIES.register(bus);
        TBRecipeSerializer.RECIPE_SERIALIZERS.register(bus);

        MinecraftForge.EVENT_BUS.register(new TBCapabilityEvents());
        MinecraftForge.EVENT_BUS.register(new TBEventHandler());
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(new TBClientEventHandler()));
    }

    static {
        HUPackSuit.registerSuitType(new ResourceLocation(TheBoys.MODID, "speedster"), SpeedsterSuit::new);
    }

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event) {
        TBNetworking.registerMessages();
    }

    @SubscribeEvent
    public void setup(final RegisterCapabilitiesEvent event) {
        event.register(IBoys.class);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TBEntities.TRAIL.get(), TrailRenderer::new);
        event.registerEntityRenderer(TBEntities.LIGHTNING_PROJECTILE.get(), LightningProjectileRenderer::new);

    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void setupClient(FMLClientSetupEvent event) {
        AbilitiesScreen.themes.add(new ResourceLocation(TheBoys.MODID, "textures/gui/themes/the_boys.png"));
        AbilitiesScreen.themes.add(new ResourceLocation(TheBoys.MODID, "textures/gui/themes/the_seven.png"));
        OverlayRegistry.registerOverlayAbove(ForgeIngameGui.HOTBAR_ELEMENT, "A-Train Overlay", new ATrainOverlay());
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void registerItemColor(ColorHandlerEvent.Item event) {
        event.getItemColors().register(InjectionItem::getColor, TBItems.INJECTION.get());
        event.getItemColors().register(InjectionItem::getColor, TBItems.VIAL.get());
    }
}