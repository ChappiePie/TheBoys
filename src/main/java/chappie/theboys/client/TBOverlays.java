package chappie.theboys.client;

import chappie.modulus.client.ClientEvents;
import chappie.modulus.client.hud.AbilityBarRenderer;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.common.ability.base.condition.KeyCondition;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.KeyMap;
import chappie.modulus.util.data.CommonAccessors;
import chappie.theboys.TheBoys;
import chappie.theboys.TheBoysClient;
import chappie.theboys.client.gui.SynthesizerScreen;
import chappie.theboys.common.ability.HeatVisionAbility;
import chappie.theboys.common.ability.SpeedAbility;
import chappie.theboys.util.TBClientUtil;
import chappie.theboys.util.TBConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix3x2fStack;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class TBOverlays {
    public static final Identifier TEXTURE = TheBoys.id("textures/gui/ui.png");
    private static final Identifier A_TRAIN = TheBoys.id("textures/gui/atrain.png");
    private static final IHasTimer.Timer APPEAR_ANIM_TICK = new IHasTimer.Timer(() -> 15, () -> false);

    private static final IHasTimer.Timer ANIM_TICK = new IHasTimer.Timer(() -> 10, TheBoysClient.OVERLAY::isDown);

    public static void render(Minecraft mc, float partialTick, GuiGraphicsExtractor GuiGraphicsExtractor) {
        AbilityBarRenderer.setEnabled(false);
        TBOverlays.renderATrain(GuiGraphicsExtractor, partialTick, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        TBOverlays.renderEyes(GuiGraphicsExtractor, mc, partialTick);
        TBOverlays.renderHud(mc, mc.gui, GuiGraphicsExtractor, partialTick, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
    }

    public static void renderHud(Minecraft mc, Gui gui, GuiGraphicsExtractor GuiGraphicsExtractor, float partialTick, int width, int height) {
        Entity entity = mc.getCameraEntity();
        if (!(entity instanceof LivingEntity e)) {
            return;
        }
        PowerCap cap = PowerCap.getCap(e);
        if (cap == null || cap.getSuperpower() == null) {
            return;
        }
        Superpower power = cap.getSuperpower();
        // color of background
        int color = ARGB.color(127, 0x282828);
        int textColor = ARGB.color(200, 0xFFFFFF);
        int x = 7;
        int y = 7;
        List<Ability> abilities = CommonUtil.getAbilities(e).stream().filter(a -> !a.isHidden() && a.getHudProperties() != null).sorted(TBOverlays::compareAbilitiesByKey).toList();
        int size = abilities.size();
        float f = ANIM_TICK.value(partialTick);
        float f1 = (float) (Math.pow(Math.cos(f * Math.PI / 2), 10) * Math.cos(f * Math.PI));
        f1 = Math.max(0, Math.min(f1, 1));
        Matrix3x2fStack matrix = GuiGraphicsExtractor.pose();

        boolean tab = f1 < 0.25F;
        //InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_TAB);

        // Text above
        MutableComponent text = (tab ? Component.translatable("overlay.theboys.abilities") :
                power.getDisplayName().copy()).withStyle(ClientUtil.BOLD_MINECRAFT);

        if (false) {
            if (tab) {
                GuiGraphicsExtractor.fill(x - 5, y + 6, x - 1, y + 16 - 6, textColor);
            } else {
                GuiGraphicsExtractor.fill(x - 3, y + 4, x - 2, y + 16 - 4, textColor);
                GuiGraphicsExtractor.fill(x - 2, y + 6, x - 1, y + 16 - 6, textColor);
            }

            GuiGraphicsExtractor.fill(x, y, x + mc.font.width(text) + 5, y + 16, color);
            GuiGraphicsExtractor.text(mc.font, text, x + 3, y + 4, textColor, true);
        } else {
            x -= 5;
            y -= 20;
        }

        // Superpower icon
        x += 7;
        y += 19;
        if (f != 1) {
            GuiGraphicsExtractor.fill(x, y, x + 22, y + 22, ARGB.color((int) (f1 * 255 / 2), color));
            if (power.iconTexture() != null) {
                GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, power.iconTexture(), x + 3, y + 3, power.iconU(), power.iconV(), 16, 16, 256, 256, ARGB.white(f1));
            }
            //GuiGraphicsExtractor.blit(TEXTURE, x + 3, y + 3, 0, 128, 16, 16, 256, 256);

            matrix.pushMatrix();
            matrix.scale(0.5F, 0.5F);
            int newX = x * 4;
            int newY = y * 2 + 30;
            MutableComponent overlayKey = TheBoysClient.OVERLAY.getTranslatedKeyMessage().copy().withStyle(ClientUtil.BOLD_MINECRAFT);
            if (f1 > 0.5F) {
                GuiGraphicsExtractor.fill(newX, newY, newX + mc.font.width(overlayKey) + 8, newY + 16, ARGB.colorFromFloat(f1 * 0.5F, 0, 0, 0));
                GuiGraphicsExtractor.text(mc.font, overlayKey, newX + 5, newY + 5, ARGB.color((int) (f1 * 255), textColor), true);
            }
            matrix.popMatrix();
        }
        // Abilities icons and text
        if (f1 != 1 && size != 0) {
            matrix.pushMatrix();

            int type = Minecraft.getInstance().gui.screen() instanceof ChatScreen ? 0 : 2;
            int maxX = 94;
            if (type == 1) {
                maxX = size * 20;
            } else if (type == 2) {
                maxX = 20;
            }
            for (Ability ability : abilities) {
                if (type == 0) {
                    int maxWidth = mc.font.width(ability.getBuilder().displayName());
                    if (maxX < maxWidth) {
                        maxX = maxWidth;
                    }
                }
            }

            float f2 = Math.min(1.0f - f1, 0.5F) * 2F;
            matrix.translate(f1 * -(maxX + (type == 2 ? 30 : 0)), 0.0F);
            //RenderSystem.setShaderColor(1, 1, 1, f2);
            GuiGraphicsExtractor.fill(x - 5, y + 2, x - 4, y + (type == 1 ? 20 : size * 20), ARGB.color((int) (f2 * 255), textColor));

            if (type == 1) {
                GuiGraphicsExtractor.fill(x, y, x + maxX + 2, y + 20 + 2, ARGB.color((int) (f2 * 255 / 2), color));
            } else {
                for (int i = 0; i < size; i++) {
                    int texY = y + i * 20;
                    GuiGraphicsExtractor.fill(x, texY, x + maxX + 2, texY + 20 + (i + 1 == size ? 2 : 0), ARGB.color((int) (f2 * 255 / 2), color));
                }
            }
            for (int i = 0; i < size; i++) {
                Ability ability = abilities.get(i);
                var hudProps = ability.getHudProperties();
                if (hudProps == null) continue;
                int j = i * 20;
                int texX = x + (type == 1 ? j : 0);
                int texY = y + (type == 1 ? 0 : j);


                {
                    int cBack = hudProps.backgroundColor();
                    float r = ARGB.red(cBack) / 255F;
                    float g = ARGB.green(cBack) / 255F;
                    float b = ARGB.blue(cBack) / 255F;

                    float alpha = !ability.isEnabled() ? 0.25F : 0.75F;
                    GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, texX + 3, texY + 3, 0, 0, 16, 16, 256, 256, ARGB.colorFromFloat(alpha, r, g, b));

                    if (ability.isEnabled()) {
                        GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, texX + 2, texY + 2, 0, 16, 18, 18, 256, 256, ARGB.colorFromFloat(alpha, r, g, b));
                    }
                }

                if (hudProps.texture() != null) {
                    GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, hudProps.texture(), texX + 3, texY + 3, hudProps.u(), hudProps.v(), hudProps.width(), hudProps.height(), 256, 256);
                }
                if (type == 0) {
                    GuiGraphicsExtractor.text(mc.font, ability.getBuilder().displayName(), x + 22, texY + 7, ARGB.color((int) (f2 * 255), textColor), true);
                }

                KeyMap.KeyType keyType = hudProps.resolveKeyType(ability);
                if (keyType != null) {
                    MutableComponent key;
                    if (!keyType.isMouse) {
                        key = ClientEvents.getMappingFromType(keyType).getTranslatedKeyMessage().copy();
                    } else {
                        key = Component.literal(keyType == KeyMap.KeyType.MOUSE_RIGHT ? "RMB" : "LMB");
                    }
                    key = key.withStyle(ClientUtil.BOLD_MINECRAFT);
                    matrix.pushMatrix();
                    matrix.scale(0.5F, 0.5F);
                    int newX = texX * 2 + 30;
                    int newY = texY * 2 + 30;
                    if (type == 1) {
                        newX = texX * 2 + 10 + 6 - mc.font.width(key) / 2;
                    }
                    GuiGraphicsExtractor.fill(newX, newY, newX + mc.font.width(key.getString().toUpperCase()) + 11, newY + 16, ARGB.colorFromFloat(127 * ((f2 == 0.25F ? 0.5F : 1F) * f2), 0, 0, 0));
                    GuiGraphicsExtractor.text(mc.font, key.getString().toUpperCase(), newX + 6, newY + 5, ARGB.color((int) (((f2 == 0.25F ? 0.5F : 1F) * f2) * 255), textColor), true);
                    matrix.popMatrix();
                }
            }
            matrix.popMatrix();
        }
    }

    public static int compareAbilitiesByKey(Ability a, Ability a1) {
        int o = Integer.MAX_VALUE;
        int o1 = Integer.MAX_VALUE;
        for (Map.Entry<String, List<Condition>> entry : a.conditionManager.methodConditions().entrySet()) {
            for (Condition condition : entry.getValue()) {
                if (condition instanceof KeyCondition key) {
                    o = key.keyType.ordinal();
                    if (entry.getKey().equals("enabling")) {
                        o--;
                    }
                    break;
                }
            }
        }
        for (Map.Entry<String, List<Condition>> entry : a1.conditionManager.methodConditions().entrySet()) {
            for (Condition condition : entry.getValue()) {
                if (condition instanceof KeyCondition key) {
                    o1 = key.keyType.ordinal();
                    if (entry.getKey().equals("enabling")) {
                        o1--;
                    }
                    break;
                }
            }
        }
        return Integer.compare(o, o1);
    }

    public static void renderEyes(GuiGraphicsExtractor GuiGraphicsExtractor, Minecraft client, float partialTick) {
        if (TBConfig.CLIENT_SPEC.isLoaded() && !TBConfig.CLIENT.eyesOverlay.get()) return;
        Entity entity = client.getCameraEntity();
        if (entity != null && entity.isAlive()) {
            if (client.options.getCameraType() == CameraType.FIRST_PERSON) {
                for (HeatVisionAbility a : CommonUtil.getAbilitiesByType(HeatVisionAbility.class, entity)) {
                    Color color = a.dataManager.get(CommonAccessors.COLOR);
                    float timer = a.glowTimer.value(partialTick);
                    int red = color.getRed(), green = color.getGreen(), blue = color.getBlue(), alpha = (int) (timer * 255);
                    if (a.isEnabled() && TBConfig.CLIENT.heatVisionHardcored.get()) {
                        float t = timer * 20;
                        if (!(t < 1.0F)) {
                            GuiGraphicsExtractor.blurBeforeThisStratum();
                        }
                    }

                    int i = ARGB.color(alpha, red, green, blue);
                    GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED
                            , TBClientUtil.GLOW_EYES_OVERLAY, 0, 0, 0.0F, 0.0F,
                            GuiGraphicsExtractor.guiWidth(), GuiGraphicsExtractor.guiHeight(), GuiGraphicsExtractor.guiWidth(), GuiGraphicsExtractor.guiHeight(), i);
                }
            }
        }
    }

    public static void renderATrain(GuiGraphicsExtractor GuiGraphicsExtractor, float partialTick, int width, int height) {
        Entity entity = Minecraft.getInstance().getCameraEntity();
        int left = width - 96;
        int top = height - 24;
        Matrix3x2fStack matrix = GuiGraphicsExtractor.pose();
        if (entity != null && entity.isAlive()) {
            for (SpeedAbility ability : CommonUtil.getAbilitiesByType(SpeedAbility.class, entity)) {
                float f = APPEAR_ANIM_TICK.value(partialTick);
                float f1 = (float) (Math.pow(Math.cos(f * Math.PI / 2), 3) * Math.cos(f * Math.PI));
                matrix.pushMatrix();
                matrix.translate(f1 * 140.0F, 0.0F);
                GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, A_TRAIN, left, top, 0, 0, 96, 24, 96, 48);
                if (ability.isEnabled()) {
                    float u = 47.5F;
                    for (float multiplier : new float[]{0.25F, 0.375F, 0.5F, 0.625F, 0.75F, 0.875F, 1.0F}) {
                        if (ability.dataManager.get(SpeedAbility.SPEED_LVL) >= (int) (ability.getMaxSpeedLevel() * multiplier)) {
                            u += 11.5F;
                        }
                    }
                    ClientUtil.blit(GuiGraphicsExtractor, A_TRAIN, left, top, 0, 24, u * 0.75F, 24, u * 0.75F, 24, 96, 48, -1);
                }
                matrix.popMatrix();
                break;
            }

        }
    }

    public static void clientTick(Minecraft minecraft) {
        if (!minecraft.isPaused()) {
            Player player = minecraft.player;
            SynthesizerScreen.rollTimer.update();
            SynthesizerScreen.timer.update();
            if (player != null && player.isAlive()) {
                APPEAR_ANIM_TICK.predicate = () -> CommonUtil.getAbilitiesByType(SpeedAbility.class, player)
                        .stream().anyMatch(Ability::isEnabled);
                APPEAR_ANIM_TICK.update();

                ANIM_TICK.update();
            }
        }
    }
}
