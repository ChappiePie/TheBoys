package chappie.theboys;

import chappie.modulus.client.ModulusClient;
import chappie.modulus.client.gui.ChappModListWidget;
import chappie.modulus.util.events.FirstPersonAdditionalHandCallback;
import chappie.modulus.util.events.SetupAnimCallback;
import chappie.theboys.client.ClientEvents;
import chappie.theboys.client.TBOverlays;
import chappie.theboys.client.gui.EyeOptionsScreen;
import chappie.theboys.client.gui.SynthesizerScreen;
import chappie.theboys.client.item.VialTintSource;
import chappie.theboys.client.model.CapeModel;
import chappie.theboys.client.renderer.TrailRenderer;
import chappie.theboys.client.renderer.block.SynthesizerRenderer;
import chappie.theboys.common.ability.base.TBSuperpower;
import chappie.theboys.common.block.entity.TBBlockEntities;
import chappie.theboys.common.block.menu.TBMenus;
import chappie.theboys.common.entity.TBEntities;
import chappie.theboys.common.particle.LaserParticleFactory;
import chappie.theboys.common.particle.TBParticleTypes;
import chappie.theboys.common.particle.WaterSplashParticle;
import chappie.theboys.networking.TBNetworking;
import chappie.theboys.util.TBClientUtil;
import chappie.theboys.util.TBConfig;
import chappie.theboys.util.conditional.HasVialDataProperty;
import chappie.theboys.util.tooltip.ArmorTooltip;
import chappie.theboys.util.tooltip.ClientArmorTooltip;
import chappie.theboys.util.tooltip.ClientSuperpowerTooltip;
import chappie.theboys.util.tooltip.SuperpowerTooltip;
import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class TheBoysClient implements ClientModInitializer {

    public static final ToggleKeyMapping OVERLAY = new ToggleKeyMapping("key.%s.overlay".formatted(TheBoys.MODID), GLFW.GLFW_KEY_LEFT_ALT, ModulusClient.MODULUS_CATEGORY, () -> TBConfig.CLIENT.abilitiesOverlayToggle.get(), false);

    static {
        ChappModListWidget.MOD_CLICKED.put(TheBoys.MODID, (e) ->
                Minecraft.getInstance().setScreen(new EyeOptionsScreen(e.parent)));
    }

    @Override
    public void onInitializeClient() {
        ItemTintSources.ID_MAPPER.put(TheBoys.id("vial"), VialTintSource.MAP_CODEC);
        ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> builder = ImmutableMap.builder();
        TBClientUtil.SUIT.putFrom(TBClientUtil.createArmorMeshSet(), builder);
        for (Map.Entry<ModelLayerLocation, LayerDefinition> value : builder.build().entrySet()) {
            EntityModelLayerRegistry.registerModelLayer(value.getKey(), value::getValue);
        }
        EntityModelLayerRegistry.registerModelLayer(CapeModel.LAYER_LOCATION, CapeModel::createBodyLayer);
        TBNetworking.registerClientMessages();
        KeyBindingHelper.registerKeyBinding(OVERLAY);
        ClientEntityEvents.ENTITY_LOAD.register((e, w) -> {
            if (e instanceof Player) {
                EyeOptionsScreen.updateData();
            }
        });
        LivingEntityFeatureRenderEvents.ALLOW_CAPE_RENDER.register(ClientEvents::capeRender);
        SetupAnimCallback.EVENT.register(ClientEvents::setupAnim);
        FirstPersonAdditionalHandCallback.EVENT.register(ClientEvents::firstPersonAdditionalHand);
        ClientTickEvents.END_CLIENT_TICK.register(TBOverlays::clientTick);
        ParticleFactoryRegistry.getInstance().register(TBParticleTypes.LASER, LaserParticleFactory::new);
        ParticleFactoryRegistry.getInstance().register(TBParticleTypes.WATER_SPLASH, WaterSplashParticle.Provider::new);
        EntityRenderers.register(TBEntities.TRAIL, TrailRenderer::new);

        ConditionalItemModelProperties.ID_MAPPER.put(TheBoys.id("has_vial"), HasVialDataProperty.MAP_CODEC);

        MenuScreens.register(TBMenus.SYNTHESIZER, SynthesizerScreen::new);
        BlockEntityRenderers.register(TBBlockEntities.SYNTHESIZER, (context) -> new SynthesizerRenderer());

        TooltipComponentCallback.EVENT.register(tooltip ->
                tooltip instanceof ArmorTooltip(ItemStack itemStack) ? new ClientArmorTooltip(itemStack) :
                        tooltip instanceof SuperpowerTooltip(TBSuperpower superpower) ? new ClientSuperpowerTooltip(superpower) : null);
    }
}
