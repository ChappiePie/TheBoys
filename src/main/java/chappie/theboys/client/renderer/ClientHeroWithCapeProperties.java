package chappie.theboys.client.renderer;

import chappie.modulus.client.model.CapeModel;
import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.render.IRenderStateEntity;
import chappie.theboys.TheBoys;
import chappie.theboys.common.ability.FlightAbility;
import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.util.ClientSuitProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import software.bernie.geckolib.object.Color;

public final class ClientHeroWithCapeProperties extends ClientSuitProperties {
    public CapeModel model;

    public ClientHeroWithCapeProperties(SuitItem suitItem) {
        super(suitItem);
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, HumanoidRenderState renderState, EquipmentSlot pSlot, int pPackedLight, ItemStack armorStack, ItemStack suitStack, HumanoidModel<?> model, float alpha) {
        super.render(pPoseStack, pBuffer, renderState, pSlot, pPackedLight, armorStack, suitStack, model, alpha);
        if (this.model == null) {
            this.model = new CapeModel(Minecraft.getInstance().getEntityModels().bakeLayer(CapeModel.LAYER_LOCATION));
        }

        if (renderState instanceof IRenderStateEntity<?> state && state.modulus$entity() instanceof AbstractClientPlayer player && pSlot == EquipmentSlot.CHEST) {
            float partialTicks = ClientUtil.getPartialTick();
            //float rotation = getRotation(player, partialTicks);

            ModelPart cape = this.model.root().getChild("cape");
            //cape.xRot = (float) Math.toRadians(rotation + 10F);
            if (renderState instanceof PlayerRenderState playerRenderState) {
                cape.rotateBy(
                        new Quaternionf()
                                .rotateY((float) -Math.PI)
                                .rotateX((6.0F + playerRenderState.capeLean / 2.0F + playerRenderState.capeFlap) * (float) (Math.PI / 180.0))
                                .rotateZ(playerRenderState.capeLean2 / 2.0F * (float) (Math.PI / 180.0))
                                .rotateY((180.0F - playerRenderState.capeLean2 / 2.0F) * (float) (Math.PI / 180.0))
                );
            }

            for (FlightAbility a : CommonUtil.listOfType(FlightAbility.class, CommonUtil.getAbilities(player))) {
                float t = a.sprintingTimer.value(partialTicks);
                //cape.xRot += (float) ((Math.toRadians(10F + Mth.sin((player.tickCount + partialTicks) / 20F) * 5F) - cape.xRot / 2F) * a.timer.value(partialTicks));
                //cape.xRot += (float) ((Math.toRadians(10F) - cape.xRot) * sprintingTimer.value(partialTicks));
                cape.xRot -= cape.xRot * t;
            }

            pPoseStack.pushPose();
            //model.body.translateAndRotate(pPoseStack);
            if (player.isCrouching()) {
                pPoseStack.translate(0, 0.15, 0.025);
                pPoseStack.mulPose(Axis.XP.rotationDegrees(22.5F));
                cape.xRot -= (float) Math.toRadians(6.125F);
            } else {
                pPoseStack.translate(0, -0.02, 0.025);
            }
            this.model.renderToBuffer(pPoseStack, pBuffer.getBuffer(this.renderType()), pPackedLight, OverlayTexture.NO_OVERLAY, Color.ofARGB(alpha, 1.0F, 1.0F, 1.0F).getColor());
            pPoseStack.popPose();
        }
    }

    public RenderType renderType() {
        return RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath(TheBoys.MODID, "textures/suits/%s/cape.png".formatted(this.type())));
    }

    private float getRotation(AbstractClientPlayer player, float partialTicks) {
        double d0 = Mth.lerp(partialTicks, player.xCloakO, player.xCloak) - Mth.lerp(partialTicks, player.xo, player.getX());
        double d1 = Mth.lerp(partialTicks, player.yCloakO, player.yCloak) - Mth.lerp(partialTicks, player.yo, player.getY());
        double d2 = Mth.lerp(partialTicks, player.zCloakO, player.zCloak) - Mth.lerp(partialTicks, player.zo, player.getZ());

        float f = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        double d3 = Mth.sin(f * (float) Math.PI / 180F);
        double d4 = -Mth.cos(f * (float) Math.PI / 180F);
        float f1 = (float) d1 * 10.0F;
        f1 = Mth.clamp(f1, -6.0F, 32.0F);
        float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
        f2 = Mth.clamp(f2, 0.0F, 150.0F);
        if (f2 < 0.0F) {
            f2 = 0.0F;
        }

        float f4 = Mth.lerp(partialTicks, player.oBob, player.bob);
        f1 += Mth.sin(Mth.lerp(partialTicks, player.walkDistO, player.walkDist) * 6.0F) * 32.0F * f4;
        return 6.0F + f2 / 2.0F + f1;
    }
}
