package chappie.theboys.client.gui;

import chappie.modulus.client.model.SuitModel;
import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.IOneScaleScreen;
import chappie.theboys.TheBoys;
import chappie.theboys.common.ability.HeatVisionAbility;
import chappie.theboys.networking.TBNetworking;
import chappie.theboys.networking.server.ServerSetEyeOptions;
import chappie.theboys.util.ISetupGameProfiles;
import chappie.theboys.util.TBConfig;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;

public class EyeOptionsScreen extends Screen implements IOneScaleScreen {

    public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(TheBoys.MODID, "textures/gui/eye_options.png");
    private static final HumanoidModel<?> EYES_LAYER_MODEL = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER));
    private static double LASERS_LENGTH = 5;

    private final LinkedList<Renderable> laserOptions = new LinkedList<>();
    private final Screen parent;

    private int tickCount, lastXofPresets = this.width / 2;
    @Nullable
    private PlayerInfo playerInfo;
    @Nullable
    private SuitModel<Player> model;
    private String skinModel;
    private ModSlider eyesLengthSlider, eyesHeightSlider, rotationSlider;
    private EditBox name;
    public final Map<Renderable, Function<Integer, Integer>> changeYPos = new HashMap<>();

    public EyeOptionsScreen(Screen screen) {
        super(Component.translatable("gui.theboys.eyeOptions"));
        this.parent = screen;
    }

    @Override
    protected void init() {
        super.init();
        this.changeYPos.clear();
        assert this.minecraft != null;
        this.addRenderableWidget(rotationSlider = new ModSlider(this.width / 2 + 80 - 15, this.height / 2 + 75, 128, 20, (slider) -> Component.translatable("gui.theboys.eyeOptions.playerRotation", slider.getValueString()), -180, 180, 0, "0"));
        this.addSkinPresets();

        PlayerInfo playerinfo = new PlayerInfo(this.minecraft.getUser().getGameProfile(), false);
        playerinfo.getSkinLocation(); // update textures and model
        this.minecraft.getPlayerSocialManager().addPlayer(playerinfo);
        this.playerInfo = playerinfo;

        {
            this.name = new EditBox(this.font, this.width / 2 + 80 - 3, this.height / 2 - 90, 106, 12, Component.translatable("gui.theboys.eyeOptions.profileName")) {
                @Override
                public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
                    if (pKeyCode == 257 || pKeyCode == 335) {
                        this.setFocused(false);
                        EyeOptionsScreen.this.changeProfile(this.getValue());
                        return true;
                    }

                    return super.keyPressed(pKeyCode, pScanCode, pModifiers);
                }

                @Override
                public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
                    boolean flag = pMouseX >= (double) this.getX() && pMouseX < (double) (this.getX() + this.width) && pMouseY >= (double) this.getY() && pMouseY < (double) (this.getY() + this.height);
                    if (this.isVisible() && pButton == 0 && !flag) {
                        EyeOptionsScreen.this.changeProfile(this.getValue());
                    }
                    return super.mouseClicked(pMouseX, pMouseY, pButton);
                }
            };
            this.name.setCanLoseFocus(true);
            this.name.setTextColor(-1);
            this.name.setTextColorUneditable(-12632257);
            this.name.setMaxLength(50);
            this.name.setHint(Component.literal(playerInfo.getProfile().getName()));
            this.addRenderableWidget(this.name);
        }

        this.changeYPos.put(this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_96257_) -> {
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 - 166, this.height / 2 + 75, 128, 20).build()), y -> y + (!laserOptions.isEmpty() ? laserOptions.size() > 3 ? 70 : 40 : 0) + 5);
    }

    @Override
    public void tick() {
        super.tick();
        this.tickCount++;
        for (Renderable renderable : this.renderables) {
            if (renderable instanceof ModSlider modSlider) {
                modSlider.tick();
            }
        }
    }

    @Override
    public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
        ModSlider eyesLengthSlider = this.eyesLengthSlider, eyesHeightSlider = this.eyesHeightSlider, rotationSlider = this.rotationSlider;
        String s = this.name != null ? this.name.getValue() : "";
        super.resize(pMinecraft, pWidth, pHeight);
        this.addLaserOptions();
        this.eyesLengthSlider.copy(eyesLengthSlider);
        this.eyesHeightSlider.copy(eyesHeightSlider);
        this.rotationSlider.copy(rotationSlider);
        this.name.setValue(s);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return this.name.keyPressed(pKeyCode, pScanCode, pModifiers) || this.name.canConsumeInput() || super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (!this.playerInfo.getModelName().equals(this.skinModel)) {
            this.skinModel = this.playerInfo.getModelName();
            this.model = new SuitModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(this.skinModel.equalsIgnoreCase("slim") ? SuitModel.SUIT_SLIM : SuitModel.SUIT));
        }
        this.setModelProperties(this.model, pPartialTick);
        this.renderDirtBackground(pPoseStack);
        pPoseStack.pushPose();

        int h = this.height / 2 + (laserOptions.isEmpty() ? 35 : laserOptions.size() < 4 ? 15 : 0);
        {
            pPoseStack.pushPose();
            float f = 2.5F;
            pPoseStack.scale(f, f, f);
            pPoseStack.translate((this.width / 2F - 200) / f, (h - 95) / f, 0);
            font.drawShadow(pPoseStack, Component.literal("The Boys").withStyle(ClientUtil.BOLD_MINECRAFT), 0, 0,
                    FastColor.ARGB32.color(255, 170, 20, 20));
            pPoseStack.popPose();
        }

        fill(pPoseStack, this.width / 2 - 200, h - 70, this.lastXofPresets, h + (!laserOptions.isEmpty() ? laserOptions.size() > 3 ? 70 : 40 : 0), 1979711488);
        fill(pPoseStack, this.width / 2 - 200, h - 72, this.lastXofPresets, h - 70, 1191182335);

        int i = this.width / 2 + 80;
        int j = this.height / 2 - 70;
        fill(pPoseStack, i, j - 2, i + 100, j, 1191182335);
        GuiComponent.enableScissor(i, j, i + 100, j + 140);
        fill(pPoseStack, i, j, i + 100, j + 140, 1979711488);
        renderEntityInInventory(pPoseStack, i + 50, j + 130, 60, (float) (i + 50) - pMouseX, (float) (j + 51) - pMouseY);
        GuiComponent.disableScissor();
        pPoseStack.popPose();
        this.changeYPos.forEach((key, value) -> {
            if (key instanceof AbstractWidget w) {
                w.setY(value.apply(h));
            }
            if (key instanceof ModLabel l) {
                l.setY(value.apply(h));
            }
        });
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    public void setModelProperties(SuitModel<Player> model, float pPartialTick) {
        if (this.minecraft == null) return;
        model.young = false;
        model.setAllVisible(true);
        model.hat.copyFrom(model.head);
        model.hat.visible = minecraft.options.isModelPartEnabled(PlayerModelPart.HAT);
        model.jacket.visible = minecraft.options.isModelPartEnabled(PlayerModelPart.JACKET);
        model.leftPants.visible = minecraft.options.isModelPartEnabled(PlayerModelPart.LEFT_PANTS_LEG);
        model.rightPants.visible = minecraft.options.isModelPartEnabled(PlayerModelPart.RIGHT_PANTS_LEG);
        model.leftSleeve.visible = minecraft.options.isModelPartEnabled(PlayerModelPart.LEFT_SLEEVE);
        model.rightSleeve.visible = minecraft.options.isModelPartEnabled(PlayerModelPart.RIGHT_SLEEVE);

        model.rightArm.xRot = model.leftArm.xRot = 0;
        model.rightArm.zRot = model.leftArm.zRot = 0;
        AnimationUtils.bobModelPart(model.rightArm, this.tickCount + pPartialTick, 1.0F);
        AnimationUtils.bobModelPart(model.leftArm, this.tickCount + pPartialTick, -1.0F);
    }

    public void renderEntityInInventory(PoseStack poseStack, int x, int y, int scale, float angleXComponent, float angleYComponent) {
        if (this.model == null) return;
        float f = (float) Math.atan(angleXComponent / 40.0F);
        float f1 = (float) Math.atan(angleYComponent / 40.0F);
        Quaternionf rotation = (new Quaternionf()).rotateZ((float) Math.PI).rotateY((float) this.rotationSlider.getValue() * ((float) Math.PI / 180F));
        Quaternionf quaternionf = (new Quaternionf()).rotateX(f1 * 20.0F * ((float) Math.PI / 180F));
        this.model.head.yRot = (f * 40.0F) * ((float) Math.PI / 180F);
        this.model.head.xRot = (-f1 * 20.0F) * ((float) Math.PI / 180F);

        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate(0.0D, 0.0D, 1000.0D);
        RenderSystem.applyModelViewMatrix();
        poseStack.pushPose();
        poseStack.translate(x, y, -950.0D);
        poseStack.mulPoseMatrix((new Matrix4f()).scaling((float) scale, (float) scale, (float) (-scale)));
        poseStack.mulPose(rotation);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (quaternionf != null) {
            quaternionf.conjugate();
            entityrenderdispatcher.overrideCameraOrientation(quaternionf);
        }

        entityrenderdispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        RenderSystem.runAsFancy(() -> {
            this.model.renderToBuffer(poseStack, multibuffersource$buffersource.getBuffer(RenderType.entityTranslucent(this.playerInfo.getSkinLocation())), 15728880, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
            this.renderEyesAndLasers(poseStack, multibuffersource$buffersource, 15728880);
        });
        multibuffersource$buffersource.endBatch();
        entityrenderdispatcher.setRenderShadow(true);
        poseStack.popPose();
        Lighting.setupFor3DItems();
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public void renderEyesAndLasers(PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn) {
        if (this.model == null || TBConfig.CLIENT.eyesType.get() == 0) return;
        float red = 1.0F, green = 0.0F, blue = 0.0F;
        poseStack.pushPose();
        this.model.head.translateAndRotate(poseStack);
        float f2 = EyeOptionsScreen.getEyesHeight() - 5;
        poseStack.translate(0, f2 * 0.0625F, 0);
        float f1 = EyeOptionsScreen.getEyesLength();
        float f3 = f1 == 1 ? 0 : f1 == 2 ? 0.0625F * 4F : 0.0625F * (8.25F - (3 - f1) * 4.25F);
        poseStack.translate(0F, f3, 0F);
        poseStack.scale(1F, f1, 1F);
        // Basically that's render of eyes without lasers
        {
            float f = 1.03125f;
            float alpha = 1;
            poseStack.pushPose();
            poseStack.scale(f, f, f);
            VertexConsumer vertexConsumer = bufferIn.getBuffer(RenderType.beaconBeam(HeatVisionAbility.GLOW_EYES, true));
            for (int i = 0; i < 3; i++) {
                poseStack.pushPose();
                poseStack.translate(0, (i == 2 ? -1 : i) / 32F, 0);
                EYES_LAYER_MODEL.head.render(poseStack, vertexConsumer, packedLightIn, OverlayTexture.NO_OVERLAY, red, green, blue, i == 0 ? alpha : alpha * 0.25F);
                poseStack.popPose();
            }
            poseStack.translate(0, 0, -(Math.cos(this.tickCount * this.tickCount) / 100F));
            EYES_LAYER_MODEL.hat.render(poseStack, vertexConsumer, packedLightIn, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
            poseStack.popPose();
        }

        // Lasers rendered via 2 boxes
        double distance = LASERS_LENGTH;
        if (distance != 0.0F) {
            for (int i = 0; i < 2; i++) {
                float x = i == 0 ? 0.15F : -0.15F;
                AABB box = new AABB(x, -0.25F, -0.25F, 0, -0.25F, -distance).inflate(0.03D);
                poseStack.pushPose();
                poseStack.scale(0.5F, 0.75F, 1);
                poseStack.translate(x, -0.05, 0);
                ClientUtil.renderFilledBox(poseStack, bufferIn.getBuffer(ClientUtil.ModRenderTypes.MAIN_LASER), box, 1F, 1F, 1F, 1, packedLightIn);
                VertexConsumer vertexConsumer = bufferIn.getBuffer(ClientUtil.ModRenderTypes.LASER);
                ClientUtil.renderFilledBox(poseStack, vertexConsumer, box.inflate(0.015D), red, green, blue, 0.2F, packedLightIn);
                ClientUtil.renderFilledBox(poseStack, vertexConsumer, box.inflate(0.03D), red, green, blue, 0.2F, packedLightIn);
                poseStack.popPose();
            }
        }
        poseStack.popPose();
    }

    public static int getEyesHeight() {
        return switch (TBConfig.CLIENT.eyesType.get()) {
            case 2 -> 6;
            case 4 -> TBConfig.CLIENT.eyesHeight.get();
            case 5 -> TBConfig.CLIENT.eyesHeight2.get();
            default -> 5;
        };
    }

    public static int getEyesLength() {
        return switch (TBConfig.CLIENT.eyesType.get()) {
            case 3 -> 2;
            case 4 -> TBConfig.CLIENT.eyesLength.get();
            case 5 -> TBConfig.CLIENT.eyesLength2.get();
            default -> 1;
        };
    }


    private void addSkinPresets() {
        int x = this.width / 2 - 200 + 6, y1 = (laserOptions.isEmpty() ? 0 : laserOptions.size() < 3 ? -5 : 0) - 60, y = this.height / 2 + 35 - 60;
        this.changeYPos.put(this.addRenderableOnly(new ModLabel(Component.translatable("gui.theboys.eyeOptions.basicPresets").withStyle(ChatFormatting.BOLD), x + 4, y, -1)), newY -> newY + y1);
        y += 16;
        for (int i = 1; i < 6; i++) {
            x += i == 1 ? 0 : 38;
            var widget = this.addRenderableWidget(new BasicSkinPresetButton(this, x, y, i));
            this.changeYPos.put(widget, newY -> newY + y1 + 16);
            if (i == TBConfig.CLIENT.eyesType.get()) {
                widget.skip = true;
                widget.onPress();
                widget.skip = false;
            }
        }
        this.lastXofPresets = x + 38;
    }

    private void addLaserOptions() {
        int x = this.width / 2 - 200 + 12, y = this.height / 2 + 35;
        int sliderWidth = 172, sliderHeight = 16;
        this.eyesHeightSlider = new ModSlider(x, y + 36, sliderWidth, sliderHeight, (slider) -> Component.translatable("gui.theboys.eyeOptions.eyeHeight", slider.getValueString()), 1, 8, this.getEyesHeight()) {

            @Override
            protected void applyValue() {
                super.applyValue();
                switch (TBConfig.CLIENT.eyesType.get()) {
                    case 4 -> TBConfig.CLIENT.eyesHeight.set((int) this.getValue());
                    case 5 -> TBConfig.CLIENT.eyesHeight2.set((int) this.getValue());
                }
                EyeOptionsScreen.updateData();
                if (eyesLengthSlider != null) {
                    eyesLengthSlider.updateMessage();
                }
            }

            @Override
            public double getValue() {
                return Mth.floor(super.getValue());
            }
        };
        this.eyesLengthSlider = new ModSlider(x, y + 54, sliderWidth, sliderHeight, (slider) -> Component.translatable("gui.theboys.eyeOptions.eyeLength", slider.getValueString()), 1, 8, this.getEyesLength(), "0") {

            @Override
            protected void applyValue() {
                super.applyValue();
                switch (TBConfig.CLIENT.eyesType.get()) {
                    case 4 -> TBConfig.CLIENT.eyesLength.set((int) this.getValue());
                    case 5 -> TBConfig.CLIENT.eyesLength2.set((int) this.getValue());
                }
                EyeOptionsScreen.updateData();
            }

            @Override
            public double maxValue() {
                if (eyesHeightSlider != null) {
                    return (eyesHeightSlider.maxValue() + 1) - eyesHeightSlider.getValue();
                }
                return super.maxValue();
            }

            @Override
            public double getValue() {
                return Mth.floor(super.getValue());
            }
        };
        if (TBConfig.CLIENT.eyesType.get() == 0) {
            return;
        }

        for (Renderable laserOption : laserOptions) {
            if (laserOption instanceof GuiEventListener listener) {
                this.changeYPos.remove(listener);
                this.removeWidget(listener);
            }
        }
        this.laserOptions.clear();
        var label = this.addRenderableOnly(new ModLabel(Component.translatable("gui.theboys.eyeOptions.laserOptions")
                .withStyle(ChatFormatting.BOLD), x + 4, y + 4, -1));
        var lasersSlider = this.addRenderableWidget(new ModSlider(x, y + 18, sliderWidth, sliderHeight,
                (slider) -> Component.translatable("gui.theboys.eyeOptions.layersLength", slider.getValueString()), 0, 5, LASERS_LENGTH, "0.0") {
            @Override
            protected void applyValue() {
                super.applyValue();
                LASERS_LENGTH = this.getValue();
            }
        });
        this.laserOptions.add(label);
        this.laserOptions.add(lasersSlider);

        if (TBConfig.CLIENT.eyesType.get() == 0 || TBConfig.CLIENT.eyesType.get() > 3) {
            this.laserOptions.add(this.addRenderableWidget(this.eyesHeightSlider));
            this.laserOptions.add(this.addRenderableWidget(this.eyesLengthSlider));

        }

        this.changeYPos.put(this.eyesHeightSlider, newY -> newY + 26);
        this.changeYPos.put(this.eyesLengthSlider, newY -> newY + 44);
        this.changeYPos.put(label, newY -> newY - 4);
        this.changeYPos.put(lasersSlider, newY -> newY + 8);
    }

    private void removeObj(Renderable renderable) {
        this.removeObj(renderable, true);
    }

    private void removeObj(Renderable renderable, boolean laserOptions) {
        this.renderables.remove(renderable);
        this.changeYPos.remove(renderable);
        if (laserOptions) {
            this.laserOptions.remove(renderable);
        }
        if (renderable instanceof GuiEventListener listener) {
            this.removeWidget(listener);
        }
    }

    private void changeProfile(String name) {
        if (minecraft == null || name.isBlank() || name.isEmpty()
                || this.playerInfo != null && this.playerInfo.getProfile().getName().equals(name)) return;
        ((ISetupGameProfiles) minecraft).theBoys$setup();

        SkullBlockEntity.updateGameprofile(new GameProfile(null, name), (newProfile) -> {
            PlayerInfo playerinfo = new PlayerInfo(newProfile, false);
            playerinfo.getSkinLocation(); // update textures and model
            EyeOptionsScreen.this.minecraft.getPlayerSocialManager().addPlayer(playerinfo);
            EyeOptionsScreen.this.playerInfo = playerinfo;
        });
    }

    @Override
    public int scale(int scale) {
        return 3;
    }

    public static void updateData() {
        if (Minecraft.getInstance().level != null) {
            TBNetworking.INSTANCE.sendToServer(new ServerSetEyeOptions(EyeOptionsScreen.getEyesHeight(), EyeOptionsScreen.getEyesLength()));
        }
    }

    public static class BasicSkinPresetButton extends ImageButton {

        protected final EyeOptionsScreen screen;
        protected final int type;
        protected boolean skip;

        public BasicSkinPresetButton(EyeOptionsScreen screen, int pX, int pY, int type) {
            this(screen, pX, pY, type, (button) -> {
                BasicSkinPresetButton b = (BasicSkinPresetButton) button;
                if (!b.skip) {
                    if (TBConfig.CLIENT.eyesType.get() == type) {
                        TBConfig.CLIENT.eyesType.set(0);
                    } else {
                        TBConfig.CLIENT.eyesType.set(type);
                    }
                    EyeOptionsScreen.updateData();
                }
                if (screen.laserOptions.isEmpty()) {
                    screen.addLaserOptions();
                }
                screen.eyesHeightSlider.setValue(getEyesHeight());
                screen.eyesLengthSlider.setValue(getEyesLength());

                screen.removeObj(screen.eyesHeightSlider);
                screen.removeObj(screen.eyesLengthSlider);
                if (TBConfig.CLIENT.eyesType.get() == 0 || TBConfig.CLIENT.eyesType.get() > 3) {
                    var eyesHeight = screen.addRenderableWidget(screen.eyesHeightSlider);
                    var eyesLength = screen.addRenderableWidget(screen.eyesLengthSlider);
                    screen.laserOptions.add(eyesHeight);
                    screen.laserOptions.add(eyesLength);
                    int y = -5 + (screen.laserOptions.size() > 3 ? -5 : 0);
                    screen.changeYPos.put(eyesHeight, newY -> newY + y + 36);
                    screen.changeYPos.put(eyesLength, newY -> newY + y + 54);
                }
                switch (TBConfig.CLIENT.eyesType.get()) {
                    case 0 -> {
                        for (Renderable laserOption : screen.laserOptions) {
                            screen.removeObj(laserOption, false);
                        }
                        screen.laserOptions.clear();
                    }
                    case 1 -> {
                        screen.eyesHeightSlider.valueToInitial();
                        screen.eyesLengthSlider.valueToInitial();
                    }
                    case 2 -> {
                        screen.eyesHeightSlider.setValue(6);
                        screen.eyesLengthSlider.valueToInitial();
                    }
                    case 3 -> {
                        screen.eyesLengthSlider.setValue(2);
                        screen.eyesHeightSlider.valueToInitial();
                    }
                    case 4, 5 -> {
                    }
                }

            });
        }

        public BasicSkinPresetButton(EyeOptionsScreen screen, int pX, int pY, int type, OnPress pOnPress) {
            super(pX, pY, 32, 32, 0, 24, 32, EyeOptionsScreen.TEXTURE_LOCATION, 128, 128, pOnPress, Component.translatable("gui.theboys.eyeOptions.skinPreset"));
            this.screen = screen;
            this.type = type;
        }

        @Override
        public void renderWidget(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            this.setFocused(TBConfig.CLIENT.eyesType.get() == this.type);
            super.renderWidget(pPoseStack, pMouseX, pMouseY, pPartialTick);
            blit(pPoseStack, this.getX() + 4, this.getY() + 4, type > 3 ? 72 : (type - 1) * 24, 0, 24, 24, 128, 128);
        }
    }

    public static class ModLabel implements Renderable {
        private final Component text;
        private int x, y;
        private final int color;

        public ModLabel(Component pText, int pX, int pY, int pColor) {
            this.text = pText;
            this.x = pX;
            this.y = pY;
            this.color = pColor;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        @Override
        public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            GuiComponent.drawString(pPoseStack, Minecraft.getInstance().font, this.text, this.x, this.y, this.color);
        }
    }
}
