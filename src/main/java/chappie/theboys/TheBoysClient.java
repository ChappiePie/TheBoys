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
import chappie.theboys.client.renderer.TrailRenderer;
import chappie.theboys.client.renderer.block.SynthesizerRenderer;
import chappie.theboys.common.block.entity.TBBlockEntities;
import chappie.theboys.common.block.menu.TBMenus;
import chappie.theboys.common.entity.TBEntities;
import chappie.theboys.common.item.SyringeItem;
import chappie.theboys.common.item.TBItems;
import chappie.theboys.common.item.VialItem;
import chappie.theboys.common.particle.LaserParticle;
import chappie.theboys.common.particle.TBParticleTypes;
import chappie.theboys.networking.TBNetworking;
import chappie.theboys.util.TBConfig;
import chappie.theboys.util.tooltip.ArmorTooltip;
import chappie.theboys.util.tooltip.ClientArmorTooltip;
import chappie.theboys.util.tooltip.ClientSuperpowerTooltip;
import chappie.theboys.util.tooltip.SuperpowerTooltip;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

public class TheBoysClient implements ClientModInitializer {

    public static final ToggleKeyMapping OVERLAY = new ToggleKeyMapping("key.%s.overlay".formatted(TheBoys.MODID), GLFW.GLFW_KEY_LEFT_ALT, "key.categories.%s".formatted(Modulus.MODID), () -> TBConfig.CLIENT.abilitiesOverlayToggle.get());

    static {
        ChappModListWidget.MOD_CLICKED.put(TheBoys.MODID, (e) ->
                Minecraft.getInstance().setScreen(new EyeOptionsScreen(e.parent)));
    }

    @Override
    public void onInitializeClient() {
        TBNetworking.registerClientMessages();
        EntityModelLayerRegistry.registerModelLayer(CapeModel.LAYER_LOCATION, CapeModel::createBodyLayer);
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
        ParticleFactoryRegistry.getInstance().register(TBParticleTypes.LASER, LaserParticle.LaserParticleFactory::new);
        EntityRendererRegistry.register(TBEntities.TRAIL, TrailRenderer::new);

        ColorProviderRegistry.ITEM.register((stack, i) -> i > 0 ? -1 : ((SyringeItem) stack.getItem()).getColor(stack), TBItems.SYRINGE);
        ColorProviderRegistry.ITEM.register((stack, i) -> i > 0 ? -1 : ((VialItem) stack.getItem()).getColor(stack), TBItems.VIAL);

        ItemProperties.register(TBItems.SYRINGE, TheBoys.id("has_vial"), (pStack, pLevel, pEntity, pSeed) -> {
            if (pStack.getTag() != null && pStack.getTag().contains("vial")) {
                return 1;
            }
            return 0;
        });

        MenuScreens.register(TBMenus.SYNTHESIZER, SynthesizerScreen::new);
        BlockEntityRenderers.register(TBBlockEntities.SYNTHESIZER,
                context -> new SynthesizerRenderer());

        TooltipComponentCallback.EVENT.register(tooltip -> {
            if (tooltip instanceof ArmorTooltip) {
                return new ClientArmorTooltip((ArmorTooltip) tooltip);
            }
            if (tooltip instanceof SuperpowerTooltip) {
                return new ClientSuperpowerTooltip((SuperpowerTooltip) tooltip);
            }
            return null;
        });
    }
}
