package chappie.theboys;

import chappie.modulus.Modulus;
import chappie.modulus.client.gui.ChappModListWidget;
import chappie.modulus.util.events.FirstPersonAdditionalHandCallback;
import chappie.modulus.util.events.SetupAnimCallback;
import chappie.theboys.client.ClientEvents;
import chappie.theboys.client.TBOverlays;
import chappie.theboys.client.gui.EyeOptionsScreen;
import chappie.theboys.client.gui.SynthesizerScreen;
import chappie.theboys.client.model.CapeModel;
import chappie.theboys.client.model.SuitModel;
import chappie.theboys.client.renderer.TrailRenderer;
import chappie.theboys.client.renderer.block.SynthesizerRenderer;
import chappie.theboys.common.block.entity.TBBlockEntities;
import chappie.theboys.common.block.menu.TBMenus;
import chappie.theboys.common.entity.TBEntities;
import chappie.theboys.common.item.SyringeItem;
import chappie.theboys.common.item.TBItems;
import chappie.theboys.common.item.VialItem;
import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.common.particle.LaserParticleFactory;
import chappie.theboys.common.particle.TBParticleTypes;
import chappie.theboys.common.particle.WaterSplashParticle;
import chappie.theboys.util.TBConfig;
import chappie.theboys.util.tooltip.ArmorTooltip;
import chappie.theboys.util.tooltip.ClientArmorTooltip;
import chappie.theboys.util.tooltip.ClientSuperpowerTooltip;
import chappie.theboys.util.tooltip.SuperpowerTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.lwjgl.glfw.GLFW;

@Mod(value = TheBoys.MODID, dist = Dist.CLIENT)
public class TheBoysClient {

    public static final ToggleKeyMapping OVERLAY = new ToggleKeyMapping(
            "key.%s.overlay".formatted(TheBoys.MODID),
            GLFW.GLFW_KEY_LEFT_ALT,
            "key.categories.%s".formatted(Modulus.MODID),
            TheBoysClient::isOverlayToggle
    );

    static {
        ChappModListWidget.MOD_CLICKED.put(TheBoys.MODID, (e) ->
                Minecraft.getInstance().setScreen(new EyeOptionsScreen(e.parent)));
    }

    public TheBoysClient(IEventBus modEventBus) {
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerEntityModelLayers);
        modEventBus.addListener(this::registerKeyMappings);
        modEventBus.addListener(this::registerParticleProviders);
        modEventBus.addListener(this::registerEntityRenderers);
        modEventBus.addListener(this::registerItemColors);
        modEventBus.addListener(this::registerMenuScreens);
        modEventBus.addListener(this::registerBlockEntityRenderers);
        modEventBus.addListener(this::registerTooltipComponents);
        modEventBus.addListener(this::registerGuiLayers);

        NeoForge.EVENT_BUS.addListener(this::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);

        SetupAnimCallback.EVENT.register(ClientEvents::setupAnim);
        FirstPersonAdditionalHandCallback.EVENT.register(ClientEvents::firstPersonAdditionalHand);
    }

    private static boolean isOverlayToggle() {
        try {
            return TBConfig.CLIENT.abilitiesOverlayToggle.get();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(this::registerItemProperties);
    }

    private void registerEntityModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SuitModel.SUIT, () -> SuitModel.createLayerDefinition(CubeDeformation.NONE, false));
        event.registerLayerDefinition(SuitModel.SUIT_SLIM, () -> SuitModel.createLayerDefinition(CubeDeformation.NONE, true));
        event.registerLayerDefinition(CapeModel.LAYER_LOCATION, CapeModel::createBodyLayer);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OVERLAY);
    }

    private void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(TBParticleTypes.LASER.get(), LaserParticleFactory::new);
        event.registerSpriteSet(TBParticleTypes.WATER_SPLASH.get(), WaterSplashParticle.Provider::new);
    }

    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TBEntities.TRAIL.get(), TrailRenderer::new);
    }

    private void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, i) -> i > 0 ? -1 : SyringeItem.getColor(stack), TBItems.SYRINGE.get());
        event.register((stack, i) -> i > 0 ? -1 : VialItem.getColor(stack), TBItems.VIAL.get());
    }

    private void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(TBMenus.SYNTHESIZER.get(), SynthesizerScreen::new);
    }

    private void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TBBlockEntities.SYNTHESIZER.get(), context -> new SynthesizerRenderer());
    }

    private void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ArmorTooltip.class, ClientArmorTooltip::new);
        event.register(SuperpowerTooltip.class, ClientSuperpowerTooltip::new);
    }

    private void registerItemProperties() {
        ItemProperties.register(TBItems.SYRINGE.get(), TheBoys.id("has_vial"), (pStack, pLevel, pEntity, pSeed) -> {
            if (pStack.has(TBDataComponents.VIAL.get())) {
                return 1;
            }
            return 0;
        });
    }

    private void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player && event.getLevel().isClientSide()) {
            EyeOptionsScreen.updateData();
        }
    }

    private void onClientTick(ClientTickEvent.Post event) {
        TBOverlays.clientTick(Minecraft.getInstance());
    }

    private void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelowAll(TheBoys.id("overlays"), TBOverlays::render);
    }
}
