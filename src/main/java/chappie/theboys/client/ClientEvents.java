package chappie.theboys.client;

import chappie.modulus.common.capability.anim.PlayerAnimCap;
import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.events.FirstPersonAdditionalHandEvent;
import chappie.modulus.util.events.RegisterPlayerControllerEvent;
import chappie.modulus.util.events.SetupAnimEvent;
import chappie.theboys.TheBoys;
import chappie.theboys.client.gui.EyeOptionsScreen;
import chappie.theboys.common.ability.FlightAbility;
import chappie.theboys.common.ability.HeatVisionAbility;
import chappie.theboys.common.ability.SuperHearingAbility;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.item.SyringeItem;
import chappie.theboys.common.item.TBItems;
import chappie.theboys.common.item.VialItem;
import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.util.interfaces.ISimpleSoundInstance;
import chappie.theboys.util.TBClientUtil;
import chappie.theboys.util.TBCommonUtil;
import chappie.theboys.util.timers.SyringeVialAnim;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.awt.*;

public class ClientEvents {

    @SubscribeEvent
    public void firstPersonAdditionalHand(FirstPersonAdditionalHandEvent event) {
        var player = event.getEntity();
        TheBoysCap theBoysCap = TheBoysCap.getCap(player);
        if (theBoysCap != null) {
            float t = theBoysCap.vialAnim.timeline.value(event.partialTicks());
            if (theBoysCap.vialAnim.hideOffHand(player, theBoysCap, event.partialTicks(), event.hand()) && t < 0.2F) {
                event.equippedProgress().set(1.0F - t * 5F);
            } else {
                if (t > 0) {
                    event.swingProgress().set(0.0F);
                    event.equippedProgress().set(0.0F);
                }
            }
        }

        PlayerAnimCap cap = PlayerAnimCap.getCap(player);
        if (cap != null) {
            var controller = cap.getController("theboys_syringe_controller", true);
            if (controller != null && controller.getCurrentAnimation() != null && controller.getAnimationState() != AnimationController.State.STOPPED) {
                if (event.hand() == InteractionHand.MAIN_HAND) {
                    event.equippedProgress().set(0.0F);
                } else if (controller.getCurrentAnimation().animation().name().equals("injecting")
                        || controller.getCurrentAnimation().animation().name().equals("inject_tick")) {
                    event.renderArm().set(true);
                }
            }
        }
    }

    @SubscribeEvent
    public <T extends LivingEntity, M extends HumanoidModel<T>> void setupAnim(SetupAnimEvent<T, M> event) {
        TheBoysCap theBoysCap = TheBoysCap.getCap(event.getEntity());
        float partialTicks = event.getModelProperties().partialTicks();
        if (theBoysCap != null && event.getEntity() instanceof Player pPlayer) {
            SyringeVialAnim vialAnim = theBoysCap.vialAnim;
            float timeline = vialAnim.timeline.value(partialTicks);

            boolean flag1 = pPlayer.getMainArm() == HumanoidArm.RIGHT;
            int i = flag1 ? 1 : -1;
            if (pPlayer.getMainHandItem().getItem() == TBItems.SYRINGE.get() && pPlayer.getOffhandItem().getItem() == TBItems.VIAL.get() || timeline > 0) {
                float t = Math.min(timeline, 0.5F) * 2F;
                float t1 = Mth.sin(pPlayer.tickCount + partialTicks) * vialAnim.rollVial.value(partialTicks);
                float t2 = vialAnim.insertVial.value(partialTicks);

                ModelPart mainHand = flag1 ? event.getModel().rightArm : event.getModel().leftArm;
                ModelPart offHand = flag1 ? event.getModel().leftArm : event.getModel().rightArm;

                float t3 = 1.0F - t;
                mainHand.xRot *= t3;
                mainHand.yRot *= t3;
                mainHand.zRot *= t3;

                offHand.xRot *= t3;
                offHand.yRot *= t3;
                offHand.zRot *= t3;

                offHand.xRot -= (float) (Math.toRadians(102.5F + t2 * 2F) * t);
                offHand.yRot += (float) (Math.toRadians(45F + t1) * t) * i;
                offHand.zRot -= (float) (Math.toRadians(85F) * t) * i;

                mainHand.xRot -= (float) (Math.toRadians(72.5F) * t);
                mainHand.yRot -= (float) (Math.toRadians(45F) * t) * i;
                mainHand.zRot += (float) (Math.toRadians(90F) * t) * i;
            }
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor()) {
                ItemStack stack = event.getEntity().getItemBySlot(slot);
                if (stack.getItem() instanceof ArmorItem && stack.getOrCreateTag().contains("Suit")) {
                    CompoundTag tag = stack.getOrCreateTag().getCompound("Suit");
                    ItemStack suitStack = ItemStack.of(tag.getCompound("Tags"));
                    if (suitStack.getItem() instanceof SuitItem item) {
                        if (event.getModelProperties().layers().stream().anyMatch(layer -> layer instanceof HumanoidArmorLayer)) {
                            Vector3f vec3f = item.getClientSuitProperties().entityWearScale(slot, event.getEntity(), stack);
                            if (slot == EquipmentSlot.HEAD) {
                                ClientUtil.modified(event.getModel().hat).setSize(vec3f);
                            }
                            if (event.getModel() instanceof PlayerModel<?> model) {
                                switch (slot) {
                                    case CHEST -> {
                                        ClientUtil.modified(model.jacket).setSize(vec3f);
                                        ClientUtil.modified(model.rightSleeve).setSize(vec3f);
                                        ClientUtil.modified(model.leftSleeve).setSize(vec3f);
                                    }
                                    case LEGS -> {
                                        ClientUtil.modified(model.jacket).setSize(vec3f);
                                        ClientUtil.modified(model.rightPants).setSize(vec3f);
                                        ClientUtil.modified(model.leftPants).setSize(vec3f);
                                    }
                                    case FEET -> {
                                        ClientUtil.modified(model.rightPants).setSize(vec3f);
                                        ClientUtil.modified(model.leftPants).setSize(vec3f);
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void playSound(PlaySoundEvent event) {
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (event.getSound() instanceof SimpleSoundInstance soundInstance && camera.getEntity() instanceof Player player) {
            for (SuperHearingAbility a : CommonUtil.listOfType(SuperHearingAbility.class, CommonUtil.getAbilities(player))) {
                if (a.isEnabled()) {
                    Vec3 vec3 = new Vec3(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ());
                    double distance = vec3.distanceTo(player.position());
                    if (soundInstance instanceof ISimpleSoundInstance sound && distance < 40) {
                        float timer = a.timer.value(Minecraft.getInstance().getPartialTick());
                        float maxVolume = ((ISimpleSoundInstance) event.getOriginalSound()).volume() * 100.0F;
                        sound.setVolume(1 + timer * maxVolume);
                        event.setSound(soundInstance);
                    }
                }
                break;
            }
        }
    }

    @SubscribeEvent
    public void setupAngles(ViewportEvent.ComputeCameraAngles event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            float partialTick = Minecraft.getInstance().getPartialTick();
            for (FlightAbility ability : CommonUtil.listOfType(FlightAbility.class, CommonUtil.getAbilities(player))) {
                float yBodyRot = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
                float f = Mth.wrapDegrees(player.getViewYRot(partialTick) - yBodyRot);
                event.setRoll((f * ability.sprintingTimer.value(partialTick)) / 2.0F);
                break;
            }
        }
    }

    @SuppressWarnings("rawtypes")
    @SubscribeEvent
    public void renderLiving(RenderLivingEvent event) {
        LivingEntity entity = event.getEntity();
        for (FlightAbility ability : CommonUtil.listOfType(FlightAbility.class, CommonUtil.getAbilities(entity))) {
            double d0 = -Mth.lerp(event.getPartialTick(), entity.xo, entity.getX());
            double d1 = -Mth.lerp(event.getPartialTick(), entity.yo, entity.getY());
            double d2 = -Mth.lerp(event.getPartialTick(), entity.zo, entity.getZ());

            if (entity instanceof Player player && !player.isSprinting()) {
                d0 += Mth.lerp(event.getPartialTick(), player.xCloakO, player.xCloak);
                d1 += Mth.lerp(event.getPartialTick(), player.yCloakO, player.yCloak);
                d2 += Mth.lerp(event.getPartialTick(), player.zCloakO, player.zCloak);
            }

            float f = ability.timer.value(event.getPartialTick());
            float f1 = ability.forwardTimer.value(event.getPartialTick());
            float f2 = ability.backwardTimer.value(event.getPartialTick());
            float f4 = ability.sprintingTimer.value(event.getPartialTick());
            float distance = Mth.sqrt((float) (d0 * d0 + d1 * d1 + d2 * d2)) * (-f2 + f1);
            float defaultRotation = Mth.clamp(distance, -1.0F, 1.0F) * 12.25F;
            float xRot = entity.getViewXRot(event.getPartialTick());
            float yRot = entity.getViewYRot(event.getPartialTick());

            if (event instanceof RenderLivingEvent.Pre) {
                event.getPoseStack().pushPose();
                event.getPoseStack().mulPose(Axis.YP.rotationDegrees(-yRot));

                boolean b = !entity.isFallFlying() && !entity.isAutoSpinAttack();
                float f5 = 1.0F - entity.getSwimAmount(event.getPartialTick());
                float f6 = b ? f5 * 75F : 0F;

                event.getPoseStack().mulPose(Axis.XP.rotationDegrees((defaultRotation + f4 * (xRot + f6)) * f));
                if (b) {
                    event.getPoseStack().translate(0.0F, -f4 * f * f5, -0.1F * f4 * f);
                }
                f4 = 1.0F - f4;
                f4 *= f;
                event.getPoseStack().translate(0, f4, 0);
                event.getPoseStack().mulPose(Axis.XP.rotationDegrees((xRot / 2F) * f4));
                event.getPoseStack().translate(0, -f4, 0);


                event.getPoseStack().mulPose(Axis.YP.rotationDegrees(yRot));
            } else {
                event.getPoseStack().popPose();
            }
            break;
        }
    }

    @SubscribeEvent
    public void renderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.options.getCameraType().isFirstPerson() || event.getHand() == InteractionHand.OFF_HAND) return;
        AbstractClientPlayer player = mc.player;
        event.getPoseStack().pushPose();
        for (HeatVisionAbility a : CommonUtil.listOfType(HeatVisionAbility.class, CommonUtil.getAbilities(player))) {
            float f = a.timer.value(event.getPartialTick());
            if (f == 0) continue;
            // remove bob
            {
                float f1 = -(player.walkDist + (player.walkDist - player.walkDistO) * event.getPartialTick());
                float f2 = Mth.lerp(event.getPartialTick(), player.oBob, player.bob);
                event.getPoseStack().mulPose(Axis.XN.rotationDegrees(Math.abs(Mth.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F));
                event.getPoseStack().mulPose(Axis.ZN.rotationDegrees(Mth.sin(f1 * (float) Math.PI) * f2 * 3.0F));
                event.getPoseStack().translate(-(Mth.sin(f1 * (float) Math.PI) * f2 * 0.5F), Math.abs(Mth.cos(f1 * (float) Math.PI) * f2), 0.0F);
            }
            Color color = a.dataManager.get(TBCommonUtil.COLOR);
            HitResult hitResult = CommonUtil.pick(player, a.dataManager.get(HeatVisionAbility.DISTANCE));
            double distance = player.getEyePosition().distanceTo(hitResult.getLocation());
            float red = color.getRed() / 255F, green = color.getGreen() / 255F, blue = color.getBlue() / 255F;
            event.getPoseStack().pushPose();
            TheBoysCap cap = TheBoysCap.getCap(player);
            if (cap != null) {
                float f1 = cap.eyesLength();
                event.getPoseStack().scale(1F, f1, 1F);
            }
            for (int i = 0; i < 2; i++) {
                AABB box = new AABB(i == 0 ? 0.1F : -0.1F, -0.25, -0.15F, 0, -0.25, -0.15F + -distance * f).inflate(0.03D);
                event.getPoseStack().pushPose();
                event.getPoseStack().translate(i == 0 ? 0.2F : -0.2F, 0.25, 0);
                ClientUtil.renderFilledBox(event.getPoseStack(), event.getMultiBufferSource().getBuffer(ClientUtil.ModRenderTypes.MAIN_LASER), box, 1F, 1F, 1F, f, event.getPackedLight());
                VertexConsumer vertexConsumer = event.getMultiBufferSource().getBuffer(ClientUtil.ModRenderTypes.LASER);
                ClientUtil.renderFilledBox(event.getPoseStack(), vertexConsumer, box.inflate(0.015D), red, green, blue, f * 0.2F, event.getPackedLight());
                ClientUtil.renderFilledBox(event.getPoseStack(), vertexConsumer, box.inflate(0.03D), red, green, blue, f * 0.2F, event.getPackedLight());
                event.getPoseStack().popPose();
            }
            event.getPoseStack().popPose();
            event.setCanceled(true);
            return;
        }
        event.getPoseStack().popPose();
    }

    @SubscribeEvent
    public void login(ClientPlayerNetworkEvent.LoggingIn event) {
        EyeOptionsScreen.updateData();
    }

    @SubscribeEvent
    public void livingTick(LivingEvent.LivingTickEvent e) {
        if (e.getEntity().isAlive() && e.getEntity() instanceof Player player) {
            PlayerAnimCap cap = PlayerAnimCap.getCap(player);
            if (cap != null) {
                if (player.isCrouching()) {
                    cap.triggerAnim("theboys_arm_controller", "dab");
                }
            }
        }
    }

    @SubscribeEvent
    public void addAnimationControllers(RegisterPlayerControllerEvent e) {
        e.registerController(b -> b.name("theboys_syringe_controller_first_person").transitionTickTime(2).animationHandler(this::handleSyringe)
                .animationFile(new ResourceLocation(TheBoys.MODID, "animations/player_first_person.animation.json")), c -> {
            //c.triggerableAnim("inject", RawAnimation.begin().then("inject", Animation.LoopType.PLAY_ONCE));
            c.setAnimationSpeed(1.5F);
            c.triggerableAnim("inject", RawAnimation.begin().thenPlay("injecting_start").thenPlay("injecting_tick"));
            c.triggerableAnim("injecting", RawAnimation.begin().thenPlay("injecting"));
            c.triggerableAnim("idk", RawAnimation.begin().thenPlay("idk"));
        });
        e.registerController(b -> b.name("theboys_syringe_controller").transitionTickTime(15).animationHandler(this::handleSyringe)
                .animationFile(new ResourceLocation(TheBoys.MODID, "animations/player.animation.json")), c -> {
            c.setAnimationSpeed(1.5F);
            c.triggerableAnim("inject", RawAnimation.begin().thenPlay("inject_tick"));
            c.triggerableAnim("inject_left", RawAnimation.begin().thenPlay("inject_tick_left"));
            c.triggerableAnim("injecting", RawAnimation.begin().thenPlay("injecting"));
        });
    }

    private PlayState handleSyringe(AnimationState<PlayerAnimCap> event) {
        Player player = event.getAnimatable().player;
        boolean thirdPerson = !event.getController().getName().contains("first_person");
        String name = thirdPerson ? (player.getMainArm() == HumanoidArm.LEFT ? "_left" : "") : "";

        if (event.getController().getCurrentAnimation() != null) {
            if (event.getController().getCurrentAnimation().animation().name().equals("injecting")) {
                event.getController().transitionLength(15);
            } else {
                if (!thirdPerson) {
                    event.getController().transitionLength(2);
                }
            }

            if (player.getUseItem().getItem() != TBItems.SYRINGE.get() || player.getUseItemRemainingTicks() <= 10) {
                if (event.getController().getAnimationState() != AnimationController.State.STOPPED) {
                    event.getController().tryTriggerAnimation("injecting");
                }
            }
        }
        return PlayState.CONTINUE;
    }
}
