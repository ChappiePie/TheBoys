package chappie.theboys.util;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.render.IRenderStateEntity;
import chappie.theboys.TheBoys;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.item.TBItems;
import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.common.item.suit.SuitItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.joml.Vector3f;

import java.util.Set;
import java.util.function.Supplier;

public class TBClientUtil {

    public static final ResourceLocation GLOW_EYES_OVERLAY = TheBoys.id("textures/gui/glow_eyes_overlay.png");

    public static final ArmorModelSet<ModelLayerLocation> SUIT = new ArmorModelSet<>(register("helmet"),
            register("chestplate"),
            register("leggings"),
            register("boots"));

    private static ModelLayerLocation register(String model) {
        return new ModelLayerLocation(TheBoys.id("suit"), model);
    }

    public static ArmorModelSet<LayerDefinition> createArmorMeshSet() {
        Supplier<MeshDefinition> meshCreator = () -> PlayerModel.createMesh(CubeDeformation.NONE, false);
        MeshDefinition head = createMeshForParts(meshCreator, "head");
        MeshDefinition body = createMeshForParts(meshCreator, "body", "left_arm", "right_arm");
        MeshDefinition leggings = createMeshForParts(meshCreator, "left_leg", "right_leg", "body");
        MeshDefinition boots = createMeshForParts(meshCreator, "left_leg", "right_leg");
        return new ArmorModelSet<>(head, body, leggings, boots).map(md -> LayerDefinition.create(md, 64, 64));
    }

    private static MeshDefinition createMeshForParts(Supplier<MeshDefinition> meshCreator, String... partNames) {
        MeshDefinition meshDefinition = meshCreator.get();
        meshDefinition.getRoot().retainPartsAndChildren(Set.of(partNames));
        return meshDefinition;
    }

    public static void setupArms(PlayerModel model, HumanoidArm side, PoseStack pPoseStack, SubmitNodeCollector renderTasks, int pCombinedLight, Player pPlayer, ModelPart pRendererArm, float partialTicks) {
        TheBoysCap cap = TheBoysCap.getCap(pPlayer);
        if (cap == null) return;
        float timeline = cap.vialAnim.timeline.value(partialTicks);
        float injectionProgress = Math.min(timeline, 0.5F) * 2F;

        boolean rightArm = side == HumanoidArm.RIGHT;
        int armSign = rightArm ? 1 : -1;
        ItemStack stack = pPlayer.getMainArm() == side ? pPlayer.getMainHandItem() : pPlayer.getOffhandItem();

        boolean vial = isVialPose(pPlayer, side, stack, timeline);
        if (stack.getItem() == TBItems.SYRINGE || vial) {
            float vialRoll = Mth.sin(pPlayer.tickCount + partialTicks) * cap.vialAnim.rollVial.value(partialTicks);
            float vialInsert = cap.vialAnim.insertVial.value(partialTicks) * 0.2F;
            applyInjectionPose(pRendererArm, pPoseStack, armSign, vial, injectionProgress, vialRoll, vialInsert);
            renderHeldInjectionItem(pPoseStack, renderTasks, pCombinedLight, pPlayer, pRendererArm, stack, rightArm, vial, injectionProgress, vialRoll, armSign);
        }

        ItemStack chestStack = pPlayer.getItemBySlot(EquipmentSlot.CHEST);
        if (!chestStack.isEmpty()) {
            renderSuitSleeveIfPresent(model, pPoseStack, renderTasks, pCombinedLight, pPlayer, pRendererArm, chestStack);
        }
    }

    public static <S extends HumanoidRenderState, A extends HumanoidModel<S>> void modifySizeOfArmor(ItemStack armorItem, EquipmentSlot slot, A model, S renderState) {
        Equippable equippable = armorItem.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.slot() == slot) {
            ItemStack suitItem = armorItem.getOrDefault(TBDataComponents.SUIT, ItemStack.EMPTY);
            if (!suitItem.isEmpty() && suitItem.getItem() instanceof SuitItem item) {
                Vector3f vec3f = item.getClientSuitProperties().armorScale(slot, armorItem);
                if (slot == EquipmentSlot.HEAD) {
                    ClientUtil.modified(model.head).modulus$setSize(vec3f);
                    ClientUtil.modified(model.hat).modulus$setSize(vec3f);
                } else if (slot == EquipmentSlot.CHEST) {

                    ClientUtil.modified(model.body).modulus$setSize(vec3f.add(0.05F, 0.05F, 0.05F, new Vector3f()));
                    if (renderState instanceof IRenderStateEntity e && CommonUtil.smallArms(e.modulus$entity())) {
                        ClientUtil.modified(model.rightArm).modulus$setSizeAndPos(vec3f.add(-0.5F, 0, 0), new Vector3f(0.5F, 0, 0));
                        ClientUtil.modified(model.leftArm).modulus$setSizeAndPos(vec3f.add(-0.5F, 0, 0), new Vector3f(-0.5F, 0, 0));
                    } else {
                        ClientUtil.modified(model.rightArm).modulus$setSize(vec3f);
                        ClientUtil.modified(model.leftArm).modulus$setSize(vec3f);
                    }
                } else if (slot == EquipmentSlot.LEGS) {
                    ClientUtil.modified(model.body).modulus$setSize(vec3f);
                    ClientUtil.modified(model.rightLeg).modulus$setSize(vec3f);
                    ClientUtil.modified(model.leftLeg).modulus$setSize(vec3f);
                } else if (slot == EquipmentSlot.FEET) {
                    ClientUtil.modified(model.rightLeg).modulus$setSize(vec3f);
                    ClientUtil.modified(model.leftLeg).modulus$setSize(vec3f);
                }
            }
        }
    }

    private static boolean isVialPose(Player player, HumanoidArm side, ItemStack stack, float timeline) {
        return stack.getItem() == TBItems.VIAL || player.getMainArm() != side && timeline > 0;
    }

    private static void applyInjectionPose(ModelPart arm, PoseStack poseStack, int armSign, boolean vial, float progress, float vialRoll, float vialInsert) {
        arm.xRot = (float) Math.toRadians(-12.5F);
        arm.yRot = (float) Math.toRadians(50F * armSign);
        arm.zRot = (float) Math.toRadians(-30.5F * armSign);
        poseStack.translate(-0.25 * armSign, 0.1, -0.1);

        if (vial) {
            arm.xRot -= (float) Math.toRadians(30F) * progress;
            arm.yRot -= (float) Math.toRadians(75.5F + vialRoll) * progress * armSign;
            arm.zRot += (float) Math.toRadians(22.5F) * progress * armSign;
            arm.x -= 0.6F * progress * armSign;
            arm.y -= (1.2F + vialInsert) * progress;
            arm.z -= (5.2F + vialInsert) * progress;
        } else {
            arm.yRot -= (float) Math.toRadians(70F) * progress * armSign;
            arm.zRot += (float) Math.toRadians(45F) * progress * armSign;
        }
    }

    private static void renderHeldInjectionItem(PoseStack poseStack, SubmitNodeCollector renderTasks, int combinedLight, Player player, ModelPart arm, ItemStack stack, boolean rightArm, boolean vial, float progress, float vialRoll, int armSign) {
        if (stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        arm.translateAndRotate(poseStack);
        poseStack.mulPose(Axis.XN.rotationDegrees(90F + (vial ? -20 : 0)));
        poseStack.mulPose(Axis.YN.rotationDegrees(180F + 11.25F * armSign));
        poseStack.translate(-0.15 * armSign, 0, -0.4);
        if (vial) {
            float scale = 1.0F - 0.4F * progress;
            poseStack.scale(scale, scale, scale);
            poseStack.translate(0, 0.155 * progress + vialRoll * 0.01F, 0);
            poseStack.mulPose(Axis.YN.rotationDegrees(vialRoll * 20));
        }
        Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().renderItem(player, stack, rightArm ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, poseStack, renderTasks, combinedLight);
        poseStack.popPose();
    }

    private static void renderSuitSleeveIfPresent(PlayerModel model, PoseStack poseStack, SubmitNodeCollector renderTasks, int combinedLight, Player player, ModelPart rendererArm, ItemStack chestStack) {
        ItemStack suitItemStack = chestStack.getOrDefault(TBDataComponents.SUIT, ItemStack.EMPTY);
        if (!(suitItemStack.getItem() instanceof SuitItem suitItem)) {
            return;
        }

        HumanoidModel<HumanoidRenderState> suitModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER));
        suitModel.leftArm.loadPose(model.leftArm.storePose());
        suitModel.rightArm.loadPose(model.rightArm.storePose());

        suitItem.getClientSuitProperties().setupSuitScale(suitModel, player, EquipmentSlot.CHEST, chestStack);
        Vector3f sleeveScale = suitItem.getClientSuitProperties().entityWearScale(EquipmentSlot.CHEST, chestStack);
        RenderType renderType = RenderType.entityTranslucent(suitItem.getClientSuitProperties().suitTexture(EquipmentSlot.CHEST, chestStack, ""));

        ClientUtil.modified(model.rightSleeve).modulus$setSize(sleeveScale);
        ClientUtil.modified(model.leftSleeve).modulus$setSize(sleeveScale);
        if (rendererArm == model.rightArm) {
            renderTasks.submitModelPart(suitModel.rightArm, poseStack, renderType, combinedLight, OverlayTexture.NO_OVERLAY, null);
        } else {
            renderTasks.submitModelPart(suitModel.leftArm, poseStack, renderType, combinedLight, OverlayTexture.NO_OVERLAY, null);
        }
    }
}
