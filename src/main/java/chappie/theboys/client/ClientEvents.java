package chappie.theboys.client;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.events.FirstPersonAdditionalHandCallback;
import chappie.modulus.util.events.SetupAnimCallback;
import chappie.theboys.client.renderer.ClientHeroWithCapeProperties;
import chappie.theboys.common.ability.FlightAbility;
import chappie.theboys.common.ability.HeatVisionAbility;
import chappie.theboys.common.ability.SuperHearingAbility;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.item.TBItems;
import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.util.TBCommonUtil;
import chappie.theboys.util.TBConfig;
import chappie.theboys.util.interfaces.ISimpleSoundInstance;
import chappie.theboys.util.timers.SyringeVialAnim;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.awt.*;

public class ClientEvents {

    public static boolean firstPersonAdditionalHand(FirstPersonAdditionalHandCallback.FirstPersonAdditionalHandEvent event) {
        var player = event.pPlayer();
        ClientEvents.renderHeatVisionFP(event.pHand(), event.pPartialTicks(), event.pMatrixStack(), event.submitNodeCollector(), event.pCombinedLight());

        TheBoysCap theBoysCap = TheBoysCap.getCap(player);
        if (theBoysCap == null) return false;
        ItemStack pStack = player.getItemInHand(event.pHand());
        if (!pStack.isEmpty() && (pStack.getItem() == TBItems.SYRINGE || pStack.getItem() == TBItems.VIAL)) {
            event.renderArm().set(true);
        }
        boolean flag1 = player.getMainArm() == HumanoidArm.RIGHT;
        int i = flag1 ? 1 : -1;
        float timeline = theBoysCap.syringeAnim.timeline.value(event.pPartialTicks());
        if (player.getMainHandItem().getItem() == TBItems.SYRINGE && timeline != 0) {
            float t = Math.min(timeline, 0.25F) * 4F;
            float t1 = 1.0F - Math.min(timeline, 0.2F) * 5F;
            if (t1 < 1.0F) {
                event.swingProgress().set(0.0F);
                event.equippedProgress().set(0.0F);
                event.renderArm().set(true);
            }
            event.pMatrixStack().translate(0, -2, -1);
            if (event.pHand() == InteractionHand.MAIN_HAND) {
                event.pMatrixStack().translate(i / 16f * t, 2 / 16f * t, -3 / 16f * t);
                event.pMatrixStack().translate(i * 8.75F / 16f, 19.75F / 16f, 6.0F / 16f);
                //event.pMatrixStack().translate(-1 * timeline, 2 * timeline, -3 * timeline);
                event.pMatrixStack().mulPose(Axis.ZP.rotationDegrees(i * -36F * t));
                event.pMatrixStack().mulPose(Axis.YP.rotationDegrees(i * 72F * t));
                event.pMatrixStack().mulPose(Axis.XP.rotationDegrees(-55F * t));
                event.pMatrixStack().translate(i * -8.75F / 16f, -19.75F / 16f, -6.0F / 16f);
            } else {
                if (player.getOffhandItem().isEmpty()) {
                    event.pMatrixStack().translate(0, -1 * t1, 0);
                }
                event.pMatrixStack().translate(i * 2 / 16f * t, 3 / 16f * t, -1 / 16f * t);
                event.pMatrixStack().translate(i * -8.75F / 16f, 19.75F / 16f, 6.0F / 16f);

                event.pMatrixStack().mulPose(Axis.ZP.rotationDegrees(i * 138F * t));
                event.pMatrixStack().mulPose(Axis.YP.rotationDegrees(i * -49F * t));
                event.pMatrixStack().mulPose(Axis.XP.rotationDegrees(-74F * t));
                event.pMatrixStack().translate(i * 8.75F / 16f, -19.75F / 16f, -6.0F / 16f);
            }
            event.pMatrixStack().translate(0, 2, 1);
        }

        float t = theBoysCap.vialAnim.timeline.value(event.pPartialTicks());
        if (t != 0F) {
            event.renderArm().set(true);
        }
        if (t > 0 && t <= 0.2F && event.pHand() == InteractionHand.OFF_HAND && pStack.isEmpty()) {
            event.equippedProgress().set(1.0F - t * 5F);
        } else {
            if (t > 0) {
                event.swingProgress().set(0.0F);
                event.equippedProgress().set(0.0F);
            }
        }
        return false;
    }

    public static void setupAnim(SetupAnimCallback.SetupAnimEvent event) {
        TheBoysCap theBoysCap = TheBoysCap.getCap(event.entity());
        float partialTicks = event.modelProperties().partialTicks();
        if (event.entity() instanceof Player pPlayer && theBoysCap != null) {
            boolean flag1 = pPlayer.getMainArm() == HumanoidArm.RIGHT;
            int i = flag1 ? 1 : -1;
            ModelPart mainHand = flag1 ? event.model().rightArm : event.model().leftArm;
            ModelPart offHand = flag1 ? event.model().leftArm : event.model().rightArm;
            if (pPlayer.getMainHandItem().getItem() == TBItems.SYRINGE) {
                float timeline = theBoysCap.syringeAnim.timeline.value(partialTicks);
                float t = Math.min(timeline, 0.25F) * 4F;

                float t3 = 1.0F - t;
                if (t3 < 1) {
                    mainHand.xRot = mainHand.xRot * t3 - (float) (Math.toRadians(90 * t));
                    mainHand.yRot = mainHand.yRot * t3 - (float) (Math.toRadians(60 * i * t));
                    mainHand.zRot = mainHand.zRot * t3 + (float) (Math.toRadians(32 * i * t));

                    offHand.xRot = offHand.xRot * t3 - (float) (Math.toRadians(90 * t));
                    offHand.yRot = offHand.yRot * t3 - (float) (Math.toRadians(40 * i * t));
                    offHand.zRot = offHand.zRot * t3 + (float) (Math.toRadians(45 * i * t));
                }
            }

            SyringeVialAnim vialAnim = theBoysCap.vialAnim;
            float timeline = vialAnim.timeline.value(partialTicks);
            if (pPlayer.getMainHandItem().getItem() == TBItems.SYRINGE && pPlayer.getOffhandItem().getItem() == TBItems.VIAL || timeline > 0) {
                float t = Math.min(timeline, 0.5F) * 2F;
                float t1 = Mth.sin(pPlayer.tickCount + partialTicks) * vialAnim.rollVial.value(partialTicks);
                float t2 = vialAnim.insertVial.value(partialTicks);

                float t3 = 1.0F - t;
                offHand.xRot = offHand.xRot * t3 - (float) Math.toRadians(105F + t2 * 4F) * t;
                offHand.yRot = offHand.yRot * t3 + (float) Math.toRadians(45F + t1) * t * i;
                offHand.zRot = offHand.zRot * t3 - (float) Math.toRadians(85F * t * i);

                mainHand.xRot = mainHand.xRot * t3 - (float) Math.toRadians(72.5F * t);
                mainHand.yRot = mainHand.yRot * t3 - (float) Math.toRadians(45F * t * i);
                mainHand.zRot = mainHand.zRot * t3 + (float) Math.toRadians(90F * t * i);
            }
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor()) {
                ItemStack stack = event.entity().getItemBySlot(slot);
                Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
                if (equippable != null && equippable.slot().isArmor() && !stack.getOrDefault(TBDataComponents.SUIT, ItemStack.EMPTY).isEmpty()) {
                    ItemStack suitStack = stack.get(TBDataComponents.SUIT);
                    if (suitStack != null && suitStack.getItem() instanceof SuitItem item) {
                        if (event.modelProperties().layers().stream().anyMatch(layer -> layer instanceof HumanoidArmorLayer)) {
                            Vector3f vec3f = item.getClientSuitProperties().entityWearScale(slot, stack);
                            if (slot == EquipmentSlot.HEAD) {
                                ClientUtil.modified(event.model().hat).modulus$setSize(vec3f);
                            }
                            if (event.model() instanceof PlayerModel model) {
                                switch (slot) {
                                    case CHEST -> {
                                        ClientUtil.modified(model.jacket).modulus$setSize(vec3f);
                                        ClientUtil.modified(model.rightSleeve).modulus$setSize(vec3f);
                                        ClientUtil.modified(model.leftSleeve).modulus$setSize(vec3f);
                                    }
                                    case LEGS -> {
                                        ClientUtil.modified(model.jacket).modulus$setSize(vec3f);
                                        ClientUtil.modified(model.rightPants).modulus$setSize(vec3f);
                                        ClientUtil.modified(model.leftPants).modulus$setSize(vec3f);
                                    }
                                    case FEET -> {
                                        ClientUtil.modified(model.rightPants).modulus$setSize(vec3f);
                                        ClientUtil.modified(model.leftPants).modulus$setSize(vec3f);
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    public static SoundInstance playSound(SoundInstance sound) {
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (sound instanceof SimpleSoundInstance soundInstance && camera.getEntity() instanceof Player player) {
            for (SuperHearingAbility a : CommonUtil.listOfType(SuperHearingAbility.class, CommonUtil.getAbilities(player))) {
                if (a.isEnabled()
                    //        || a.dataManager.get(SuperHearingAbility.RECEIVED) > 0
                ) {
                    Vec3 vec3 = new Vec3(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ());
                    double distance = vec3.distanceTo(player.position());
                    if (soundInstance instanceof ISimpleSoundInstance iSound && distance < 40) { // @TODO make new distance and base distance for homelander
                        float maxVolume = ((ISimpleSoundInstance) sound).theBoys$volume() * 100.0F;
                        iSound.theBoys$setVolume(1 + maxVolume);
                        return soundInstance;
                    }
                }
                break;
            }
        }
        return sound;
    }

    public static void setupRoll(float partialTick, PoseStack pPoseStack) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            for (FlightAbility ability : CommonUtil.listOfType(FlightAbility.class, CommonUtil.getAbilities(player))) {
                float yBodyRot = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
                float f = Mth.wrapDegrees(player.getViewYRot(partialTick) - yBodyRot);
                if (!ability.cooldown.end() && ability.dataManager.get(FlightAbility.BOOSTING) && player.isSprinting() && !Minecraft.getInstance().isPaused()) {
                    float f1 = ability.cooldown.value(partialTick);
                    pPoseStack.mulPose(Axis.ZP.rotationDegrees(player.getRandom().nextFloat() * 4 * f1));
                    pPoseStack.mulPose(Axis.YP.rotationDegrees(player.getRandom().nextFloat() * 4 * f1));

                }

                pPoseStack.mulPose(Axis.ZP.rotationDegrees((f * ability.sprintingTimer.value(partialTick)) / 2.0F));
                break;
            }
        }
    }

    public static void flightAnimation(LivingEntity entity, float partialTicks, PoseStack poseStack) {
        for (FlightAbility ability : CommonUtil.listOfType(FlightAbility.class, CommonUtil.getAbilities(entity))) {
            double d0 = -Mth.lerp(partialTicks, entity.xo, entity.getX());
            double d1 = -Mth.lerp(partialTicks, entity.yo, entity.getY());
            double d2 = -Mth.lerp(partialTicks, entity.zo, entity.getZ());

            if (entity instanceof AbstractClientPlayer clientPlayer && !clientPlayer.isSprinting()) {
                var avatarState = clientPlayer.avatarState();
                d0 += avatarState.getInterpolatedCloakX(partialTicks);
                d1 += avatarState.getInterpolatedCloakY(partialTicks);
                d2 += avatarState.getInterpolatedCloakZ(partialTicks);
            }

            float f = ability.timer.value(partialTicks);
            float f1 = ability.forwardTimer.value(partialTicks);
            float f2 = ability.backwardTimer.value(partialTicks);
            float f4 = ability.sprintingTimer.value(partialTicks);
            float distance = Mth.sqrt((float) (d0 * d0 + d1 * d1 + d2 * d2)) * (-f2 + f1);
            float defaultRotation = Mth.clamp(distance, -1.0F, 1.0F) * 12.25F;
            float xRot = entity.getViewXRot(partialTicks);
            float yRot = entity.getViewYRot(partialTicks);

            poseStack.mulPose(Axis.YP.rotationDegrees(-yRot));

            boolean b = !entity.isFallFlying() && !entity.isAutoSpinAttack();
            float f5 = 1.0F - entity.getSwimAmount(partialTicks);
            float f6 = b ? f5 * 75F : 0F;

            poseStack.mulPose(Axis.XP.rotationDegrees((defaultRotation + f4 * (xRot + f6)) * f));
            if (b) {
                poseStack.translate(0.0F, -f4 * f * f5, -0.1F * f4 * f);
            }
            f4 = 1.0F - f4;
            f4 *= f;
            poseStack.translate(0, f4, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees((xRot / 2F) * f4));
            poseStack.translate(0, -f4, 0);


            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            break;
        }
    }

    public static void renderHeatVisionFP(InteractionHand hand, float partialTicks, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        TheBoysCap cap = TheBoysCap.getCap(player);
        if (player == null || cap == null || !mc.options.getCameraType().isFirstPerson() || hand == InteractionHand.OFF_HAND)
            return;

        poseStack.pushPose();
        for (HeatVisionAbility a : CommonUtil.listOfType(HeatVisionAbility.class, CommonUtil.getAbilities(player))) {
            float f = a.timer.value(partialTicks);
            if (f == 0) continue;
            // remove bob
            {
                ClientAvatarState clientAvatarState = player.avatarState();
                float f1 = clientAvatarState.getBackwardsInterpolatedWalkDistance(partialTicks);
                float g = clientAvatarState.getInterpolatedBob(partialTicks);
                poseStack.mulPose(Axis.XN.rotationDegrees(Math.abs(Mth.cos(f1 * (float) Math.PI - 0.2F) * g) * 5.0F));
                poseStack.mulPose(Axis.ZN.rotationDegrees(Mth.sin(f1 * (float) Math.PI) * g * 3.0F));
                poseStack.translate(-Mth.sin(f1 * (float) Math.PI) * g * 0.5F, Math.abs(Mth.cos(f1 * (float) Math.PI) * g), 0.0F);
            }
            Color color = a.dataManager.get(TBCommonUtil.COLOR);
            HitResult hitResult = CommonUtil.pick(player, a.dataManager.get(HeatVisionAbility.DISTANCE));
            double distance = player.getEyePosition().distanceTo(hitResult.getLocation());
            float red = color.getRed() / 255F, green = color.getGreen() / 255F, blue = color.getBlue() / 255F;
            poseStack.pushPose();
            poseStack.scale(1F, cap.eyesLength(), 1F);
            for (int i = 0; i < 2; i++) {
                float f1 = i == 0 ? 0.1F : -0.1F;
                AABB box = new AABB(f1, -0.25, -0.15F, 0, -0.25, -0.15F + -distance * f).inflate(0.03D);
                poseStack.pushPose();
                poseStack.translate(f1 + (TBConfig.CLIENT.heatVisionHardcored.get() ? 0 : f1), 0.25, 0);
                submitNodeCollector.submitCustomGeometry(poseStack, ClientUtil.ModRenderTypes.MAIN_LASER, (pose, buffer) ->
                        ClientUtil.renderFilledBox(pose.pose(), buffer, box, 1F, 1F, 1F, f, packedLight));
                submitNodeCollector.submitCustomGeometry(poseStack, ClientUtil.ModRenderTypes.LASER, (pose, buffer) -> {
                    ClientUtil.renderFilledBox(pose.pose(), buffer, box.inflate(0.015D), red, green, blue, f * 0.2F, packedLight);
                    ClientUtil.renderFilledBox(pose.pose(), buffer, box.inflate(0.03D), red, green, blue, f * 0.2F, packedLight);
                });
                poseStack.popPose();
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    public static boolean capeRender(AvatarRenderState playerRenderState) {
        ItemStack stack = playerRenderState.chestEquipment;
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (!stack.isEmpty() && equippable != null && equippable.slot().isArmor()) {
            if (stack.getOrDefault(TBDataComponents.SUIT, ItemStack.EMPTY).getItem() instanceof SuitItem item) {
                return !(item.getClientSuitProperties() instanceof ClientHeroWithCapeProperties);
            }
        }
        return true;
    }
}
