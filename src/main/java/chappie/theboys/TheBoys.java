package chappie.theboys;

import chappie.theboys.abilities.SpeedsterSuit;
import chappie.theboys.client.TBClientEventHandler;
import chappie.theboys.common.TBEventHandler;
import chappie.theboys.common.capability.BoysCap;
import chappie.theboys.common.capability.IBoys;
import chappie.theboys.common.entities.TBEntities;
import chappie.theboys.common.items.InjectionItem;
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
        AbilityHelper.addTheme(new ResourceLocation(TheBoys.MODID, "textures/gui/themes/the_boys.png"));
        AbilityHelper.addTheme(new ResourceLocation(TheBoys.MODID, "textures/gui/themes/the_seven.png"));
        ItemModelsProperties.register(TBItems.INJECTION, new ResourceLocation(TheBoys.MODID, "injection"),
                (stack, clientWorld, livingEntity) -> InjectionItem.getCompoundV(stack) ? 1.0F : 0F);
    }
}