package chappie.theboys;

import chappie.theboys.abilities.SpeedsterSuit;
import chappie.theboys.client.TBClientEventHandler;
import chappie.theboys.common.TBEventHandler;
import chappie.theboys.common.capability.BoysCap;
import chappie.theboys.common.capability.IBoys;
import chappie.theboys.common.entities.TBEntities;
import chappie.theboys.common.items.TBItems;
import chappie.theboys.common.items.VialItem;
import chappie.theboys.network.TBNetworking;
import chappie.theboys.util.TBRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
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
import xyz.heroesunited.heroesunited.common.capabilities.HUCapStorage;
import xyz.heroesunited.heroesunited.hupacks.HUPackSuit;

@Mod(TheBoys.MODID)
public class TheBoys {
    public static final String MODID = "theboys";
    public static final Logger LOGGER = LogManager.getLogger();

    public TheBoys() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);

        TBItems.ITEMS.register(bus);
        TBRecipeSerializer.RECIPE_SERIALIZERS.register(bus);

        MinecraftForge.EVENT_BUS.register(new TBEventHandler());
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(new TBClientEventHandler()));
    }

    static {
        HUPackSuit.registerSuitType(new ResourceLocation(TheBoys.MODID, "speedster"), SpeedsterSuit::new);
    }

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event) {
        TBNetworking.registerMessages();
        CapabilityManager.INSTANCE.register(IBoys.class, new HUCapStorage(), () -> new BoysCap(null));
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void setupClient(FMLClientSetupEvent event) {
        TBEntities.EntityRenderers();
        AbilitiesScreen.themes.add(new ResourceLocation(TheBoys.MODID, "textures/gui/themes/the_boys.png"));
        AbilitiesScreen.themes.add(new ResourceLocation(TheBoys.MODID, "textures/gui/themes/the_seven.png"));
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void registerItemColor(ColorHandlerEvent.Item event) {
        event.getItemColors().register((stack, color) -> VialItem.getColor(stack, color), TBItems.INJECTION);
        event.getItemColors().register((stack, color) -> VialItem.getColor(stack, color), TBItems.VIAL);
    }
}