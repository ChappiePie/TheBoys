package chappie.theboys;

import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.events.FirstPersonAdditionalHandCallback;
import chappie.modulus.util.events.RegisterPlayerControllerCallback;
import chappie.modulus.util.events.SetupAnimCallback;
import chappie.theboys.client.ATrainOverlay;
import chappie.theboys.client.ClientEvents;
import chappie.theboys.client.gui.EyeOptionsScreen;
import chappie.theboys.client.renderer.TrailRenderer;
import chappie.theboys.common.ability.HeatVisionAbility;
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
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.awt.*;

public class TheBoysClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TBNetworking.registerClientMessages();
        ClientEntityEvents.ENTITY_LOAD.register((e, w) -> {
            if (e instanceof Player) {
                EyeOptionsScreen.updateData();
            }
        });
        RegisterPlayerControllerCallback.EVENT.register(ClientEvents::addAnimationControllers);
        SetupAnimCallback.EVENT.register(ClientEvents::setupAnim);
        FirstPersonAdditionalHandCallback.EVENT.register(ClientEvents::firstPersonAdditionalHand);
        ClientTickEvents.END_CLIENT_TICK.register(ATrainOverlay::clientTick);
        HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
            Minecraft client = Minecraft.getInstance();
            ATrainOverlay.render(client.gui, matrices, tickDelta, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
        });

        HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
            Minecraft client = Minecraft.getInstance();
            ATrainOverlay.render(client.gui, matrices, tickDelta, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
            if (TBConfig.CLIENT_SPEC.isLoaded() && !TBConfig.CLIENT.eyesOverlay.get()) return;
            Entity entity = client.getCameraEntity();
            if (entity != null && entity.isAlive()) {
                if (client.options.getCameraType() == CameraType.FIRST_PERSON) {
                    for (HeatVisionAbility a : CommonUtil.listOfType(HeatVisionAbility.class, CommonUtil.getAbilities(entity))) {
                        Color color = a.dataManager.get(TBCommonUtil.COLOR);
                        float red = color.getRed() / 255F, green = color.getGreen() / 255F, blue = color.getBlue() / 255F, alpha = a.eyesTimer.value(tickDelta);

                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.disableDepthTest();
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        RenderSystem.setShader(GameRenderer::getPositionTexShader);
                        TBClientUtil.renderTextureOverlay(TBClientUtil.GLOW_EYES_OVERLAY, client.getWindow().getGuiScaledHeight(), client.getWindow().getGuiScaledWidth(), red, green, blue, alpha);
                    }
                }
            }
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
