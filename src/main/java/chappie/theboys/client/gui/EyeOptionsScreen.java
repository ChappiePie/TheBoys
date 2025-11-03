package chappie.theboys.client.gui;

import chappie.modulus.networking.ModNetworking;
import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.IOneScaleScreen;
import chappie.theboys.TheBoys;
import chappie.theboys.mixin.client.ScreenAccessor;
import chappie.theboys.networking.server.ServerSetEyeOptions;
import chappie.theboys.util.TBConfig;
import chappie.theboys.util.interfaces.ISetupGameProfiles;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerModelType;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class EyeOptionsScreen extends Screen implements IOneScaleScreen {

    public static final ResourceLocation TEXTURE_LOCATION = TheBoys.id("textures/gui/eye_options.png");
    private static double LASERS_LENGTH = 5;
    public final Map<Renderable, Function<Integer, Integer>> changeYPos = new HashMap<>();
    private final LinkedList<Renderable> laserOptions = new LinkedList<>();
    private final Screen parent;
    private int tickCount, lastXofPresets = this.width / 2;
    @Nullable
    private PlayerInfo playerInfo;
    @Nullable
    private PlayerModel model;
    private PlayerModelType skinModel;
    private ModSlider eyesLengthSlider, eyesHeightSlider, rotationSlider;
    private EditBox name;

    public EyeOptionsScreen(Screen screen) {
        super(Component.translatable("gui.theboys.eyeOptions"));
        this.parent = screen;
    }

    public static void updateData() {
        if (Minecraft.getInstance().level != null) {
            ModNetworking.sendToServer(new ServerSetEyeOptions(EyeOptionsScreen.getEyesHeight(), EyeOptionsScreen.getEyesLength()));
        }
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

    @Override
    protected void init() {
        super.init();
        this.changeYPos.clear();
        assert this.minecraft != null;
        this.addRenderableWidget(rotationSlider = new ModSlider(this.width / 2 + 80 - 15, this.height / 2 + 75, 128, 20, (slider) -> Component.translatable("gui.theboys.eyeOptions.playerRotation", slider.getValueString()), -180, 180, 0, "0"));
        this.addSkinPresets();

        PlayerInfo playerinfo = new PlayerInfo(this.minecraft.getGameProfile(), false);
        playerinfo.getSkin(); // update textures and model
        this.minecraft.getPlayerSocialManager().addPlayer(playerinfo);
        this.playerInfo = playerinfo;

        {
            this.name = new EditBox(this.font, this.width / 2 + 80 - 3, this.height / 2 - 90, 106, 12, Component.translatable("gui.theboys.eyeOptions.profileName")) {
                @Override
                public boolean keyPressed(KeyEvent event) {
                    if (event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER) {
                        this.setFocused(false);
                        EyeOptionsScreen.this.changeProfile(this.getValue());
                        return true;
                    }

                    return super.keyPressed(event);
                }

                @Override
                public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
                    boolean flag = event.x() >= (double) this.getX() && event.x() < (double) (this.getX() + this.width) && event.y() >= (double) this.getY() && event.y() < (double) (this.getY() + this.height);
                    if (this.isVisible() && event.button() == 0 && !flag) {
                        EyeOptionsScreen.this.changeProfile(this.getValue());
                    }
                    return super.mouseClicked(event, isDoubleClick);
                }
            };
            this.name.setCanLoseFocus(true);
            this.name.setTextColor(-1);
            this.name.setTextColorUneditable(-12632257);
            this.name.setMaxLength(50);
            this.name.setHint(Component.literal(playerInfo.getProfile().name()));
            this.addRenderableWidget(this.name);
        }

        this.changeYPos.put(this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_96257_) -> {
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 - 166, this.height / 2 + 75, 128, 20).build()), y -> y + (!laserOptions.isEmpty() ? laserOptions.size() > 3 ? 70 : 40 : 0) + 5);
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
    public boolean keyPressed(KeyEvent event) {
        return this.name.keyPressed(event) || this.name.canConsumeInput() || super.keyPressed(event);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.playerInfo != null) {
            PlayerModelType currentModel = this.playerInfo.getSkin().model();
            if (this.skinModel != currentModel) {
                this.skinModel = currentModel;
                boolean slim = currentModel == PlayerModelType.SLIM;
                this.model = new PlayerModel(Minecraft.getInstance().getEntityModels().bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim);
            }
        }
        this.setModelProperties(this.model, pPartialTick);
        Matrix3x2fStack matrix = guiGraphics.pose();

        if (this.minecraft.level == null) {
            this.renderPanorama(guiGraphics, pPartialTick);
        }

        this.renderBlurredBackground(guiGraphics);
        this.renderMenuBackground(guiGraphics);

        int h = this.height / 2 + (laserOptions.isEmpty() ? 35 : laserOptions.size() < 4 ? 15 : 0);
        {
            float f = 2.5F;
            matrix.pushMatrix();
            matrix.scale(f, f);
            matrix.translate((this.width / 2F - 200) / f, (h - 95) / f);
            guiGraphics.drawString(this.font, Component.translatable("title.theboys").withStyle(ClientUtil.BOLD_MINECRAFT), 0, 0,
                    ARGB.color(255, 170, 20, 20), true);
            matrix.popMatrix();
        }

        guiGraphics.fill(this.width / 2 - 200, h - 70, this.lastXofPresets, h + (!laserOptions.isEmpty() ? laserOptions.size() > 3 ? 70 : 40 : 0), 1979711488);
        guiGraphics.fill(this.width / 2 - 200, h - 72, this.lastXofPresets, h - 70, 1191182335);

        int i = this.width / 2 + 80;
        int j = this.height / 2 - 70;
        guiGraphics.fill(i, j - 2, i + 100, j, 1191182335);
        guiGraphics.enableScissor(i, j, i + 100, j + 140);
        guiGraphics.fill(i, j, i + 100, j + 140, 1979711488);
        renderEntityInInventory(guiGraphics, i + 50, j + 130, 60, (float) (i + 50) - pMouseX, (float) (j + 51) - pMouseY);
        guiGraphics.disableScissor();
        this.changeYPos.forEach((key, value) -> {
            if (key instanceof AbstractWidget w) {
                w.setY(value.apply(h));
            }
            if (key instanceof ModLabel l) {
                l.setY(value.apply(h));
            }
        });
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }

    public void setModelProperties(PlayerModel model, float pPartialTick) {
        if (this.minecraft == null || model == null) return;
        model.setAllVisible(true);
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

    public void renderEntityInInventory(GuiGraphics guiGraphics, int x, int y, int scale, float angleXComponent, float angleYComponent) {
        if (this.playerInfo == null) {
            return;
        }

        PlayerModelType modelType = this.playerInfo.getSkin().model();
        if (this.model == null || this.skinModel != modelType) {
            boolean slim = modelType == PlayerModelType.SLIM;
            this.model = new PlayerModel(Minecraft.getInstance().getEntityModels().bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim);
            this.skinModel = modelType;
        }

        float yaw = (float) (Math.atan(angleXComponent / 40.0F) * 40.0F) + (float) this.rotationSlider.getValue();
        float pitch = -(float) Math.atan(angleYComponent / 40.0F) * 20.0F;
        this.model.head.yRot = yaw * ((float) Math.PI / 180F);
        this.model.head.xRot = pitch * ((float) Math.PI / 180F);

        ResourceLocation texture = this.playerInfo.getSkin().body().texturePath();
        int width = 100;
        int height = 140;
        guiGraphics.submitSkinRenderState(
                this.model,
                texture,
                (float) scale,
                pitch,
                yaw,
                -1.5F,
                x - width / 2,
                y - height,
                x + width / 2,
                y
        );

        this.model.head.yRot = 0.0F;
        this.model.head.xRot = 0.0F;
    }

    public void renderEyesAndLasers() {
        // TODO: Re-implement custom eye rendering for 1.21.10 render pipeline.
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
                widget.onPress(new KeyEvent(InputConstants.KEY_SPACE, 0, 0));
                widget.skip = false;
            }
        }
        this.lastXofPresets = x + 38;
    }

    private void addLaserOptions() {
        int x = this.width / 2 - 200 + 12, y = this.height / 2 + 35;
        int sliderWidth = 172, sliderHeight = 16;
        this.eyesHeightSlider = new ModSlider(x, y + 36, sliderWidth, sliderHeight, (slider) -> Component.translatable("gui.theboys.eyeOptions.eyeHeight", slider.getValueString()), 1, 8, getEyesHeight()) {

            @Override
            protected void applyValue() {
                super.applyValue();
                int value = (int) this.getValue();
                switch (TBConfig.CLIENT.eyesType.get()) {
                    case 4 -> {
                        if (!TBConfig.CLIENT.eyesHeight.get().equals(value)) {
                            TBConfig.CLIENT.eyesHeight.set(value);
                        }
                    }
                    case 5 -> {
                        if (!TBConfig.CLIENT.eyesHeight2.get().equals(value)) {
                            TBConfig.CLIENT.eyesHeight2.set(value);
                        }
                    }
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
        this.eyesLengthSlider = new ModSlider(x, y + 54, sliderWidth, sliderHeight, (slider) -> Component.translatable("gui.theboys.eyeOptions.eyeLength", slider.getValueString()), 1, 8, getEyesLength(), "0") {

            @Override
            protected void applyValue() {
                super.applyValue();
                int value = (int) this.getValue();
                switch (TBConfig.CLIENT.eyesType.get()) {
                    case 4 -> {
                        if (!TBConfig.CLIENT.eyesLength.get().equals(value)) {
                            TBConfig.CLIENT.eyesLength.set(value);
                        }
                    }
                    case 5 -> {
                        if (!TBConfig.CLIENT.eyesLength2.get().equals(value)) {
                            TBConfig.CLIENT.eyesLength2.set(value);
                        }
                    }
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

    @Override
    public void tick() {
        super.tick();
        this.tickCount++;
        for (GuiEventListener renderable : this.children()) {
            if (renderable instanceof ModSlider modSlider) {
                modSlider.tick();
            }
        }
    }

    private void changeProfile(String name) {
        if (minecraft == null || name.isBlank() || name.isEmpty()
                || this.playerInfo != null && this.playerInfo.getProfile().name().equals(name)) return;
        ((ISetupGameProfiles) minecraft).theBoys$setup();

        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        PlayerInfo playerinfo = new PlayerInfo(profile, false);
        playerinfo.getSkin(); // update textures and model
        EyeOptionsScreen.this.minecraft.getPlayerSocialManager().addPlayer(playerinfo);
        EyeOptionsScreen.this.playerInfo = playerinfo;
    }

    @Override
    public int scale(int scale) {
        return 3;
    }

    private void removeObj(Renderable renderable, boolean laserOptions) {
        ((ScreenAccessor) this).renderables().remove(renderable);
        this.changeYPos.remove(renderable);
        if (laserOptions) {
            this.laserOptions.remove(renderable);
        }
        if (renderable instanceof GuiEventListener listener) {
            this.removeWidget(listener);
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
            super(pX, pY, 32, 32, new WidgetSprites(EyeOptionsScreen.TEXTURE_LOCATION, EyeOptionsScreen.TEXTURE_LOCATION), pOnPress, Component.translatable("gui.theboys.eyeOptions.skinPreset"));
            this.screen = screen;
            this.type = type;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            this.setFocused(TBConfig.CLIENT.eyesType.get() == this.type);
            //super.renderWidget(guiGraphics, pMouseX, pMouseY, pPartialTick);
            ResourceLocation resourcelocation = this.sprites.get(this.isActive(), this.isHoveredOrFocused());
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, resourcelocation, this.getX(), this.getY(), 0, 24 + (this.isHoveredOrFocused() ? 32 : 0), 32, 32, 128, 128);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, resourcelocation, this.getX() + 4, this.getY() + 4, type > 3 ? 72 : (type - 1) * 24, 0, 24, 24, 128, 128);
        }
    }

    public static class ModLabel implements Renderable {
        private final Component text;
        private final int color;
        private int x, y;

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
        public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            guiGraphics.drawString(Minecraft.getInstance().font, this.text, this.x, this.y, this.color);
        }
    }
}
