package chappie.theboys.client;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.TheBoys;
import chappie.theboys.common.ability.SpeedAbility;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.List;

@Mod.EventBusSubscriber(modid = TheBoys.MODID, value = Dist.CLIENT)
public class ATrainOverlay implements IGuiOverlay {
    private static final ResourceLocation TEXTURE = new ResourceLocation(TheBoys.MODID, "textures/gui/atrain.png");
    private static int appearAnimTick, appearAnimTickO;

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int width, int height) {
        Entity entity = gui.getMinecraft().getCameraEntity();
        int left = width - 96;
        int top = height - 24;
        PoseStack poseStack = guiGraphics.pose();
        if (entity != null && entity.isAlive()) {
            for (SpeedAbility ability : CommonUtil.listOfType(SpeedAbility.class, CommonUtil.getAbilities(entity))) {
                float f = Mth.lerp(partialTick, ATrainOverlay.appearAnimTickO, ATrainOverlay.appearAnimTick) / 15F;
                float f1 = (float) (Math.pow(Math.cos(f * Math.PI / 2), 3) * Math.cos(f * Math.PI));
                poseStack.pushPose();
                poseStack.translate(f1 * 140.0F, 0, 0);
                guiGraphics.blit(TEXTURE, left, top, 0, 0, 96, 24, 96, 48);
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

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (!Minecraft.getInstance().isPaused() && event.phase == TickEvent.Phase.START) {
            Player player = Minecraft.getInstance().player;
            if (player != null && player.isAlive()) {
                List<SpeedAbility> abilities = CommonUtil.listOfType(SpeedAbility.class, CommonUtil.getAbilities(player));
                ATrainOverlay.appearAnimTickO = ATrainOverlay.appearAnimTick;
                if (abilities.isEmpty() && ATrainOverlay.appearAnimTick != 0) {
                    ATrainOverlay.appearAnimTick = 0;
                }
                for (SpeedAbility ability : abilities) {
                    if (ability.isEnabled() && ATrainOverlay.appearAnimTick < 15) {
                        ATrainOverlay.appearAnimTick++;
                    }
                    if (!ability.isEnabled() && ATrainOverlay.appearAnimTick != 0) {
                        ATrainOverlay.appearAnimTick--;
                    }
                }
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
