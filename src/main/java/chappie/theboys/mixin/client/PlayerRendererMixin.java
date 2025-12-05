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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, AvatarRenderState, PlayerModel> {
    @Unique
    private ModelPart theboys$currentHand;

    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(
            method = "renderHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/model/geom/ModelPart;Z)V",
            at = @At("HEAD")
    )
    private void theboys$rememberHand(PoseStack poseStack, SubmitNodeCollector renderTasks, int packedLight, ResourceLocation skinTexture, ModelPart arm, boolean sleeveVisible, CallbackInfo ci) {
        this.theboys$currentHand = arm;
    }

    @Inject(
            method = "renderHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/model/geom/ModelPart;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"
            )
    )
    private void theboys$modifyHand(PoseStack poseStack, SubmitNodeCollector renderTasks, int packedLight, ResourceLocation skinTexture, ModelPart arm, boolean sleeveVisible, CallbackInfo ci) {
        AbstractClientPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        PlayerModel playerModel = this.getModel();
        HumanoidArm side = arm == playerModel.rightArm ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        TBClientUtil.setupArms(playerModel, side, poseStack, renderTasks, packedLight, player, arm, ClientUtil.getPartialTick());
    }

    @WrapOperation(
            method = "renderHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/model/geom/ModelPart;Z)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/model/geom/ModelPart;zRot:F",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void theboys$keepOtherHandTwist(ModelPart modelPart, float value, Operation<Void> original) {
        if (modelPart == this.theboys$currentHand) {
            original.call(modelPart, value);
        }
    }
}
