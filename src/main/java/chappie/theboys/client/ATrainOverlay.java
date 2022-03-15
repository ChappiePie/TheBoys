package chappie.theboys.client;

import chappie.theboys.TheBoys;
import chappie.theboys.abilities.SpeedAbility;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;

import java.util.List;

@Mod.EventBusSubscriber(modid = TheBoys.MODID, value = Dist.CLIENT)
public class ATrainOverlay implements IIngameOverlay {
    private static int appearAnimTick, appearAnimTickO;

    @Override
    public void render(ForgeIngameGui gui, PoseStack poseStack, float partialTick, int width, int height) {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.isAlive()) {
            for (SpeedAbility ability : AbilityHelper.getListOfType(SpeedAbility.class, AbilityHelper.getAbilities(player))) {
                final ResourceLocation location = new ResourceLocation(TheBoys.MODID, "textures/gui/atrain.png");
                int left = width - 96;
                int top = height - 24;

                float val = Mth.lerp(partialTick, ATrainOverlay.appearAnimTickO, ATrainOverlay.appearAnimTick) / 15F;
                float appearAnim = (float) (Math.pow(Math.cos(val * Math.PI / 2), 3) * Math.cos(val * Math.PI));
                poseStack.pushPose();
                poseStack.translate(appearAnim * 140.0F, 0, 0);
                RenderSystem.setShaderTexture(0, location);
                GuiComponent.blit(poseStack, left, top, 0, 0, 96, 24, 96, 48);
                if (ability.getEnabled()) {
                    float speedLvl = ability.getDataManager().getAsInt("speedLevel");
                    float f = 47.5F;
                    for (float multiplier : new float[]{0.25F, 0.375F, 0.5F, 0.625F, 0.75F, 0.875F, 1.0F}) {
                        if (speedLvl >= (int) (ability.getMaxSpeedLevel() * multiplier)) {
                            f += 11.5;
                        }
                    }
                    blit(poseStack, left, top, 0, 24, f * 0.75F, 24, 96, 48);
                }
                poseStack.popPose();
                break;
            }

        }
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (!Minecraft.getInstance().isPaused() && event.phase == TickEvent.Phase.START) {
            Player player = Minecraft.getInstance().player;
            if (player != null && player.isAlive()) {
                List<SpeedAbility> abilities = AbilityHelper.getListOfType(SpeedAbility.class, AbilityHelper.getAbilities(player));
                ATrainOverlay.appearAnimTickO = ATrainOverlay.appearAnimTick;
                if (abilities.isEmpty() && ATrainOverlay.appearAnimTick != 0) {
                    ATrainOverlay.appearAnimTick = 0;
                }
                for (SpeedAbility ability : abilities) {
                    if (ability.getEnabled() && ATrainOverlay.appearAnimTick < 15) {
                        ATrainOverlay.appearAnimTick++;
                    }
                    if (!ability.getEnabled() && ATrainOverlay.appearAnimTick != 0) {
                        ATrainOverlay.appearAnimTick--;
                    }
                }
            }
        }
    }

    public static void blit(PoseStack pPoseStack, float pX, float pY, float pUOffset, float pVOffset, float pUWidth, float pVHeight, float pTextureHeight, float pTextureWidth) {
        innerBlit(pPoseStack, pX, pX + pUWidth, pY, pY + pVHeight, pUWidth, pVHeight, pUOffset, pVOffset, pTextureHeight, pTextureWidth);
    }

    private static void innerBlit(PoseStack pPoseStack, float pX1, float pX2, float pY1, float pY2, float pUWidth, float pVHeight, float pUOffset, float pVOffset, float pTextureWidth, float pTextureHeight) {
        innerBlit(pPoseStack.last().pose(), pX1, pX2, pY1, pY2, (pUOffset + 0.0F) / pTextureWidth, (pUOffset + pUWidth) / pTextureWidth, (pVOffset + 0.0F) / pTextureHeight, (pVOffset + pVHeight) / pTextureHeight);
    }

    private static void innerBlit(Matrix4f pMatrix, float pX1, float pX2, float pY1, float pY2, float pMinU, float pMaxU, float pMinV, float pMaxV) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(pMatrix, pX1, pY2, 1.0F).uv(pMinU, pMaxV).endVertex();
        bufferbuilder.vertex(pMatrix, pX2, pY2, 1.0F).uv(pMaxU, pMaxV).endVertex();
        bufferbuilder.vertex(pMatrix, pX2, pY1, 1.0F).uv(pMaxU, pMinV).endVertex();
        bufferbuilder.vertex(pMatrix, pX1, pY1, 1.0F).uv(pMinU, pMinV).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
    }
}
