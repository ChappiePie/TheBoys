package chappie.theboys;

import chappie.theboys.abilities.SpeedsterSuit;
import chappie.theboys.client.TBClientEventHandler;
import chappie.theboys.common.TBEventHandler;
import chappie.theboys.common.capability.BoysCap;
import chappie.theboys.common.capability.IBoys;
import chappie.theboys.common.entities.TBEntities;
import chappie.theboys.common.items.TBItems;
import chappie.theboys.network.TBNetworking;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;
import xyz.heroesunited.heroesunited.hupacks.HUPackSuit;

@Mod(TheBoys.MODID)
public class TheBoys {
    public static final String MODID = "theboys";
    public static final Logger LOGGER = LogManager.getLogger();

    public TheBoys() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);

        TBItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        bus.addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new TBEventHandler());
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(new TBClientEventHandler()));
    }

    static {
        HUPackSuit.registerSuitType(new ResourceLocation(TheBoys.MODID, "speedster"), SpeedsterSuit::new);
    }

    private void setup(final FMLCommonSetupEvent event) {
        TBNetworking.registerMessages();
        CapabilityManager.INSTANCE.register(IBoys.class, new BoysCap.BoysStorage(), () -> new BoysCap(null));
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void setupClient(FMLClientSetupEvent event) {
        TBEntities.EntityRenderers();
        AbilityHelper.addTheme(new ResourceLocation(TheBoys.MODID, "textures/gui/themes/the_boys.png"));
        AbilityHelper.addTheme(new ResourceLocation(TheBoys.MODID, "textures/gui/themes/the_seven.png"));

        ItemModelsProperties.register(TBItems.VIAL, new ResourceLocation(TheBoys.MODID, "vial"),
                (stack, clientWorld, livingEntity) ->
                        TBItems.VIAL.getCompoundV(stack) ? 1.0F : 0F);
    }
}