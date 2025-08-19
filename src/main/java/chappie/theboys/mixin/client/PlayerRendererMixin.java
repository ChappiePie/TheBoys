package chappie.theboys.mixin.client;

import chappie.modulus.util.ClientUtil;
import chappie.theboys.util.TBClientUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> {

    @Unique
    private AbstractClientPlayer player;

    public PlayerRendererMixin(EntityRendererProvider.Context pContext, PlayerModel pModel, float pShadowRadius) {
        super(pContext, pModel, pShadowRadius);
    }

    @WrapOperation(method = "renderHand", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/geom/ModelPart;zRot:F", opcode = Opcodes.PUTFIELD))
    private void removeArmWearRotation(ModelPart instance, float value, Operation<Void> original) {

    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V", at = @At("TAIL"))
    private void extractPlayer(AbstractClientPlayer abstractClientPlayer, PlayerRenderState playerRenderState, float f, CallbackInfo ci) {
        this.player = abstractClientPlayer;

    }

    @Inject(method = "renderHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/model/geom/ModelPart;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;entityTranslucent(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    private void renderHandPost(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, ResourceLocation skinTexture, ModelPart arm, boolean isSleeveVisible, CallbackInfo ci) {
        HumanoidArm side = arm == this.getModel().rightArm ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        TBClientUtil.setupArms(this.getModel(), side, pMatrixStack, pBuffer, pCombinedLight, Minecraft.getInstance().player, arm, null, ClientUtil.getPartialTick());
    }
}