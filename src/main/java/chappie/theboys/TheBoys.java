package chappie.theboys;

import chappie.modulus.client.gui.ChappModListWidget;
import chappie.modulus.util.CommonUtil;
import chappie.theboys.client.ATrainOverlay;
import chappie.theboys.client.ClientEvents;
import chappie.theboys.client.gui.EyeOptionsScreen;
import chappie.theboys.client.renderer.SyringeRenderer;
import chappie.theboys.client.renderer.TrailRenderer;
import chappie.theboys.common.CommonEvents;
import chappie.theboys.common.ability.HeatVisionAbility;
import chappie.theboys.common.ability.base.TBAbilityTypes;
import chappie.theboys.common.ability.base.TBSuperpowers;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.entity.TBEntities;
import chappie.theboys.common.item.SyringeItem;
import chappie.theboys.common.item.TBItems;
import chappie.theboys.common.item.VialItem;
import chappie.theboys.common.particle.LaserParticle;
import chappie.theboys.common.particle.TBParticleTypes;
import chappie.theboys.networking.TBNetworking;
import chappie.theboys.util.TBClientUtil;
import chappie.theboys.util.TBCommonUtil;
import chappie.theboys.util.TBConfig;
import chappie.theboys.util.tooltip.ArmorTooltip;
import chappie.theboys.util.tooltip.ClientArmorTooltip;
import com.mojang.logging.LogUtils;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import software.bernie.example.registry.ItemRegistry;
import software.bernie.geckolib.GeckoLib;

import java.awt.*;

@Mod(TheBoys.MODID)
public class TheBoys {

    public static final String MODID = "theboys";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheBoys() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);
        TBItems.ITEMS.register(bus);
        TBAbilityTypes.ABILITIES.register(bus);
        TBSuperpowers.SUPERPOWERS.register(bus);
        TBEntities.ENTITIES.register(bus);
        TBParticleTypes.PARTICLES.register(bus);

        MinecraftForge.EVENT_BUS.register(new CommonEvents());

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, TBConfig.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TBConfig.COMMON_SPEC);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                MinecraftForge.EVENT_BUS.register(new ClientEvents()));
    }

    static {
        ChappModListWidget.MOD_CLICKED.put(MODID, (e) -> {
            Minecraft.getInstance().setScreen(new EyeOptionsScreen(e.parent));
        });
    }

    @SubscribeEvent
    public void commonSetup(final FMLCommonSetupEvent event) {
        TBNetworking.registerMessages();
    }

    @SubscribeEvent
    public void gatherComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ArmorTooltip.class, ClientArmorTooltip::new);
    }

    @SubscribeEvent
    public void creativeModeTab(CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(new ResourceLocation(MODID, "theboys"), (e) ->
                e.icon(() -> new ItemStack(TBItems.HOMELANDER_SUIT.get(ArmorItem.Type.CHESTPLATE).get()))
                .title(Component.translatable("itemGroup.%s.theboys".formatted(MODID)))
                .displayItems((itemDisplayParameters, output) -> {
            for (RegistryObject<Item> entry : TBItems.ITEMS.getEntries()) {
                output.accept(entry.get());
            }
        }));
    }

    @SubscribeEvent
    public void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.register(TheBoysCap.class);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void registerLayerDefinitions(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        //event.registerLayerDefinition(CapeModel.LAYER_LOCATION, CapeModel::createBodyLayer);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(TBItems.SYRINGE.get(), new ResourceLocation(TheBoys.MODID, "has_vial"), (pStack, pLevel, pEntity, pSeed) -> {
            if (pStack.getTag() != null && pStack.getTag().contains("vial")) {
                return 1;
            }
            return 0;
        }));
        //event.registerLayerDefinition(CapeModel.LAYER_LOCATION, CapeModel::createBodyLayer);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void registerColor(RegisterColorHandlersEvent.Item event) {
        event.register((stack, i) -> i > 0 ? -1 : ((SyringeItem) stack.getItem()).getColor(stack), TBItems.SYRINGE.get());
        event.register((stack, i) -> i > 0 ? -1 : ((VialItem) stack.getItem()).getColor(stack), TBItems.VIAL.get());
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onParticleFactoryRegistration(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(TBParticleTypes.LASER.get(), LaserParticle.LaserParticleFactory::new);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void registerGuiOverlay(final RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "a-train", new ATrainOverlay());
        event.registerAbove(VanillaGuiOverlay.FROSTBITE.id(), "glow_eyes", (gui, mStack, partialTicks, width, height) -> {
            if (TBConfig.CLIENT_SPEC.isLoaded() && !TBConfig.CLIENT.eyesOverlay.get()) return;
            Entity entity = gui.getMinecraft().getCameraEntity();
            if (entity != null && entity.isAddedToWorld() && entity.isAlive()) {
                if (gui.getMinecraft().options.getCameraType() == CameraType.FIRST_PERSON) {
                    for (HeatVisionAbility a : CommonUtil.listOfType(HeatVisionAbility.class, CommonUtil.getAbilities(entity))) {
                        Color color = a.dataManager.get(TBCommonUtil.COLOR);
                        float red = color.getRed() / 255F, green = color.getGreen() / 255F, blue = color.getBlue() / 255F, alpha = a.eyesTimer.value(partialTicks);

                        gui.setupOverlayRenderState(true, false);
                        TBClientUtil.renderTextureOverlay(TBClientUtil.GLOW_EYES_OVERLAY, height, width, red, green, blue, alpha);
                    }
                }
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TBEntities.TRAIL.get(), TrailRenderer::new);
    }
}
