package chappie.theboys.client;

import chappie.modulus.client.ClientEvents;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.common.ability.base.condition.KeyCondition;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.KeyMap;
import chappie.theboys.TheBoys;
import chappie.theboys.TheBoysClient;
import chappie.theboys.common.ability.HeatVisionAbility;
import chappie.theboys.common.ability.SpeedAbility;
import chappie.theboys.common.ability.base.TBSuperpower;
import chappie.theboys.common.ability.interfaces.IHasOverlay;
import chappie.theboys.util.TBClientUtil;
import chappie.theboys.util.TBCommonUtil;
import chappie.theboys.util.TBConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class TBOverlays {
    public static final ResourceLocation TEXTURE = new ResourceLocation(TheBoys.MODID, "textures/gui/ui.png");
    private static final ResourceLocation A_TRAIN = new ResourceLocation(TheBoys.MODID, "textures/gui/atrain.png");
    private static final IHasTimer.Timer APPEAR_ANIM_TICK = new IHasTimer.Timer(() -> 15, () -> false);

    private static final IHasTimer.Timer ANIM_TICK = new IHasTimer.Timer(() -> 10, TheBoysClient.OVERLAY::isDown);

    public static void render(Minecraft mc, Gui gui, GuiGraphics guiGraphics, float partialTick, int width, int height) {
        Entity entity = mc.getCameraEntity();
        if (!(entity instanceof LivingEntity e) || PowerCap.getCap(e) == null || !(PowerCap.getCap(e).getSuperpower() instanceof TBSuperpower power))
            return;
        // color of background
        int color = FastColor.ABGR32.color(127, 0x282828);
        int textColor = FastColor.ABGR32.color(200, 0xFFFFFF);
        int x = 7;
        int y = 7;
        List<Ability> abilities = CommonUtil.getAbilities(e).stream().filter(a -> IHasOverlay.getInstance(a) != null).sorted(TBOverlays::compareAbilitiesByKey).toList();
        int size = abilities.size();
        float f = ANIM_TICK.value(partialTick);
        float f1 = (float) (Math.pow(Math.cos(f * Math.PI / 2), 10) * Math.cos(f * Math.PI));
        PoseStack poseStack = guiGraphics.pose();

        boolean tab = f1 < 0.25F;
        //InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_TAB);

        // Text above
        MutableComponent text;
        if (tab) {
            text = Component.translatable("theboys.overlay.abilities").withStyle(ClientUtil.BOLD_MINECRAFT);
        } else {
            text = power.getDisplayName().copy().withStyle(ClientUtil.BOLD_MINECRAFT);
            //text = Component.translatable("theboys.overlay.superpower");
        }

        if (tab) {
            guiGraphics.fill(x - 5, y + 6, x - 1, y + 16 - 6, textColor);
        } else {
            guiGraphics.fill(x - 3, y + 4, x - 2, y + 16 - 4, textColor);
            guiGraphics.fill(x - 2, y + 6, x - 1, y + 16 - 6, textColor);
        }

        guiGraphics.fill(x, y, x + mc.font.width(text) + 5, y + 16, color);
        guiGraphics.drawString(mc.font, text, x + 3, y + 5, textColor, true);

        // Superpower icon
        x += 7;
        y += 19;
        if (f != 1) {
            guiGraphics.setColor(1, 1, 1, f1);
            guiGraphics.fill(x, y, x + 22, y + 22, color);
            RenderSystem.enableBlend();
            power.renderIcon(x + 3, y + 3, f1, mc, gui, guiGraphics, partialTick, width, height);
            //guiGraphics.blit(TEXTURE, x + 3, y + 3, 0, 128, 16, 16, 256, 256);

            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 1F);
            int newX = x * 4;
            int newY = y * 3;
            var l = Language.getInstance();
            text = TheBoysClient.OVERLAY.getTranslatedKeyMessage().copy().withStyle(ClientUtil.BOLD_MINECRAFT);
            guiGraphics.fill(newX, newY, newX + mc.font.width(text) + 10, newY + 16, FastColor.ARGB32.color(127, 0, 0, 0));
            guiGraphics.drawString(mc.font, text, newX + 5, newY + 5, textColor, true);
            poseStack.popPose();
            guiGraphics.setColor(1, 1, 1, 1);
        }

        // Abilities icons and text
        if (f1 != 1) {
            poseStack.pushPose();
            int type = 1;
            int maxX = 94;
            boolean enabled = false;
            if (type == 1) {
                maxX = size * 20;
            } else if (type == 2) {
                maxX = 20;
            }
            for (Ability ability : abilities) {
                if (type == 0) {
                    int maxWidth = mc.font.width(ability.builder.displayName());
                    if (maxX < maxWidth) {
                        maxX = maxWidth;
                    }
                }
                if (ability.isEnabled()) {
                    enabled = true;
                }
            }

            float f2 = Math.min(1.0f - f1, 0.5F) * 2F;
            poseStack.translate(f1 * -maxX, 0, 0);
            guiGraphics.setColor(1, 1, 1, f2);
            guiGraphics.fill(x - 5, y + 2, x - 4, y + (type == 1 ? 20 : size * 20), textColor);

            if (type == 1) {
                guiGraphics.fill(x, y, x + maxX + 2, y + 20 + 2, color);
            } else {
                for (int i = 0; i < size; i++) {
                    int texY = y + i * 20;
                    guiGraphics.fill(x, texY, x + maxX + 2, texY + 20 + (i + 1 == size ? 2 : 0), color);
                }
            }
            for (int i = 0; i < size; i++) {
                Ability ability = abilities.get(i);
                IHasOverlay iHasOverlay = IHasOverlay.getInstance(ability);
                if (iHasOverlay == null) continue;
                int j = i * 20;
                int texX = x + (type == 1 ? j : 0);
                int texY = y + (type == 1 ? 0 : j);


                float a = enabled ? ability.isEnabled() ? 1 : 0.25F : 1;
                a *= f2;
                {
                    int cBack = iHasOverlay.getBackgroundColor();
                    float r = FastColor.ABGR32.red(cBack) / 255F;
                    float g = FastColor.ABGR32.green(cBack) / 255F;
                    float b = FastColor.ABGR32.blue(cBack) / 255F;

                    guiGraphics.setColor(r, g, b, a);
                    RenderSystem.enableBlend();
                    guiGraphics.blit(TEXTURE, texX + 3, texY + 3, 0, 0, 16, 16, 256, 256);
                    guiGraphics.setColor(1, 1, 1, a);
                }

                iHasOverlay.renderIcon(texX + 3, texY + 3, f1, mc, gui, guiGraphics, partialTick, width, height);
                if (type == 0) {
                    guiGraphics.drawString(mc.font, ability.builder.displayName(), x + 22, texY + 7, textColor, true);
                }

                guiGraphics.setColor(1, 1, 1, (a == 0.25F ? 0.5F : 1F) * f2);
                KeyMap.KeyType keyType = iHasOverlay.getKeyType();
                if (keyType != null) {
                    MutableComponent key;
                    if (!keyType.isMouse) {
                        key = ClientEvents.getMappingFromType(keyType).getTranslatedKeyMessage().copy();
                    } else {
                        key = Component.literal(keyType == KeyMap.KeyType.MOUSE_RIGHT ? "RMB" : "LMB");
                    }
                    key = key.withStyle(ClientUtil.BOLD_MINECRAFT);
                    poseStack.pushPose();
                    poseStack.translate(0, 0, 10);
                    poseStack.scale(0.5F, 0.5F, 1F);
                    int newX = texX * 4;
                    int newY = texY * 2 + 30;
                    if (type == 1) {
                        newX = texX * 2 + 10 + 6 - mc.font.width(key) / 2;
                    }
                    guiGraphics.fill(newX, newY, newX + mc.font.width(key) + 10, newY + 16, FastColor.ARGB32.color(127, 0, 0, 0));
                    poseStack.translate(0, 0, 0);
                    guiGraphics.drawString(mc.font, key.getString().toUpperCase(), newX + 6, newY + 5, textColor, true);
                    poseStack.popPose();
                }
                guiGraphics.setColor(1, 1, 1, 1);
            }
            poseStack.popPose();
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

    public static void renderEyes(Minecraft client, float partialTick) {
        if (TBConfig.CLIENT_SPEC.isLoaded() && !TBConfig.CLIENT.eyesOverlay.get()) return;
        Entity entity = client.getCameraEntity();
        if (entity != null && entity.isAlive()) {
            if (client.options.getCameraType() == CameraType.FIRST_PERSON) {
                for (HeatVisionAbility a : CommonUtil.listOfType(HeatVisionAbility.class, CommonUtil.getAbilities(entity))) {
                    Color color = a.dataManager.get(TBCommonUtil.COLOR);
                    float red = color.getRed() / 255F, green = color.getGreen() / 255F, blue = color.getBlue() / 255F, alpha = a.eyesTimer.value(partialTick);

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.disableDepthTest();
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    TBClientUtil.renderTextureOverlay(TBClientUtil.GLOW_EYES_OVERLAY, client.getWindow().getGuiScaledHeight(), client.getWindow().getGuiScaledWidth(), red, green, blue, alpha);
                }
            }
        }
    }

    public static void renderATrain(GuiGraphics guiGraphics, float partialTick, int width, int height) {
        Entity entity = Minecraft.getInstance().getCameraEntity();
        int left = width - 96;
        int top = height - 24;
        PoseStack poseStack = guiGraphics.pose();
        if (entity != null && entity.isAlive()) {
            for (SpeedAbility ability : CommonUtil.listOfType(SpeedAbility.class, CommonUtil.getAbilities(entity))) {
                float f = APPEAR_ANIM_TICK.value(partialTick);
                float f1 = (float) (Math.pow(Math.cos(f * Math.PI / 2), 3) * Math.cos(f * Math.PI));
                poseStack.pushPose();
                poseStack.translate(f1 * 140.0F, 0, 0);
                guiGraphics.blit(A_TRAIN, left, top, 0, 0, 96, 24, 96, 48);
                if (ability.isEnabled()) {
                    float u = 47.5F;
                    for (float multiplier : new float[]{0.25F, 0.375F, 0.5F, 0.625F, 0.75F, 0.875F, 1.0F}) {
                        if (ability.dataManager.get(SpeedAbility.SPEED_LVL) >= (int) (ability.getMaxSpeedLevel() * multiplier)) {
                            u += 11.5F;
                        }
                    }
                    blit(poseStack, left, top, u * 0.75F);
                }
                poseStack.popPose();
                break;
            }

        }
    }

    public static void clientTick(Minecraft minecraft) {
        if (!Minecraft.getInstance().isPaused()) {
            Player player = Minecraft.getInstance().player;
            if (player != null && player.isAlive()) {
                List<SpeedAbility> abilities = CommonUtil.listOfType(SpeedAbility.class, CommonUtil.getAbilities(player));
                boolean b = false;
                for (SpeedAbility ability : abilities) {
                    if (ability.isEnabled()) {
                        b = true;
                        break;
                    }
                }
                boolean finalB = b;
                APPEAR_ANIM_TICK.predicate = () -> finalB;
                APPEAR_ANIM_TICK.update();

                ANIM_TICK.update();
            }
        }
    }

    private static void blit(PoseStack pPoseStack, float pX, float pY, float pUWidth) {
        Matrix4f pMatrix = pPoseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(pMatrix, pX, pY + 24F, 1.0F).uv(0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(pMatrix, pX + pUWidth, pY + 24F, 1.0F).uv(pUWidth / 96F, 1.0F).endVertex();
        bufferbuilder.vertex(pMatrix, pX + pUWidth, pY, 1.0F).uv(pUWidth / 96F, 0.5F).endVertex();
        bufferbuilder.vertex(pMatrix, pX, pY, 1.0F).uv(0.0F, 0.5F).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }
}
