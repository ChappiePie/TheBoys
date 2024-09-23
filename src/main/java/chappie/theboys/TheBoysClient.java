package chappie.theboys;

import chappie.modulus.Modulus;
import chappie.modulus.util.events.FirstPersonAdditionalHandCallback;
import chappie.modulus.util.events.RegisterPlayerControllerCallback;
import chappie.modulus.util.events.SetupAnimCallback;
import chappie.theboys.client.ClientEvents;
import chappie.theboys.client.TBOverlays;
import chappie.theboys.client.gui.EyeOptionsScreen;
import chappie.theboys.client.renderer.TrailRenderer;
import chappie.theboys.common.entity.TBEntities;
import chappie.theboys.common.item.SyringeItem;
import chappie.theboys.common.item.TBItems;
import chappie.theboys.common.item.VialItem;
import chappie.theboys.common.particle.LaserParticle;
import chappie.theboys.common.particle.TBParticleTypes;
import chappie.theboys.networking.TBNetworking;
import chappie.theboys.util.TBConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

public class TheBoysClient implements ClientModInitializer {

    public static final ToggleKeyMapping OVERLAY = new ToggleKeyMapping("%s.key.overlay".formatted(TheBoys.MODID), GLFW.GLFW_KEY_LEFT_ALT, "key.categories.%s".formatted(Modulus.MODID), () -> TBConfig.CLIENT.abilitiesOverlayToggle.get());

    @Override
    public void onInitializeClient() {
        TBNetworking.registerClientMessages();
        KeyBindingHelper.registerKeyBinding(OVERLAY);
        ClientEntityEvents.ENTITY_LOAD.register((e, w) -> {
            if (e instanceof Player) {
                EyeOptionsScreen.updateData();
            }
        });
        RegisterPlayerControllerCallback.EVENT.register(ClientEvents::addAnimationControllers);
        SetupAnimCallback.EVENT.register(ClientEvents::setupAnim);
        FirstPersonAdditionalHandCallback.EVENT.register(ClientEvents::firstPersonAdditionalHand);
        ClientTickEvents.END_CLIENT_TICK.register(TBOverlays::clientTick);
        HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
            Minecraft mc = Minecraft.getInstance();
            TBOverlays.render(mc, mc.gui, matrices, tickDelta, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
            TBOverlays.renderEyes(mc, tickDelta);
            TBOverlays.renderATrain(matrices, tickDelta, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        });

        ParticleFactoryRegistry.getInstance().register(TBParticleTypes.LASER, LaserParticle.LaserParticleFactory::new);
        EntityRendererRegistry.register(TBEntities.TRAIL, TrailRenderer::new);

        ColorProviderRegistry.ITEM.register((stack, i) -> i > 0 ? -1 : ((SyringeItem) stack.getItem()).getColor(stack), TBItems.SYRINGE);
        ColorProviderRegistry.ITEM.register((stack, i) -> i > 0 ? -1 : ((VialItem) stack.getItem()).getColor(stack), TBItems.VIAL);

        ItemProperties.register(TBItems.SYRINGE, new ResourceLocation(TheBoys.MODID, "has_vial"), (pStack, pLevel, pEntity, pSeed) -> {
            if (pStack.getTag() != null && pStack.getTag().contains("vial")) {
                return 1;
            }
            return 0;
        });
    }
}
