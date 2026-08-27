package chappie.theboys.client.gui;

import chappie.modulus.networking.ModNetworking;
import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.IOneScaleScreen;
import chappie.theboys.TheBoys;
import chappie.theboys.client.gui.render.state.LaserPreviewRenderState;
import chappie.theboys.mixin.client.ScreenAccessor;
import chappie.theboys.networking.server.ServerSetEyeOptions;
import chappie.theboys.util.TBConfig;
import chappie.theboys.util.interfaces.ISetupGameProfiles;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Services;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerModelType;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class EyeOptionsScreen extends Screen implements IOneScaleScreen {

    public static final Identifier TEXTURE_LOCATION = TheBoys.id("textures/gui/eye_options.png");
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
    private final AtomicInteger profileRequestCounter = new AtomicInteger();

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

    private static UUID generateOfflineUuid(String playerName) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void resize(int pWidth, int pHeight) {
        ModSlider eyesLengthSlider = this.eyesLengthSlider, eyesHeightSlider = this.eyesHeightSlider, rotationSlider = this.rotationSlider;
        String s = this.name != null ? this.name.getValue() : "";
        super.resize(pWidth, pHeight);
        this.addLaserOptions();
        this.eyesLengthSlider.copy(eyesLengthSlider);
        this.eyesHeightSlider.copy(eyesHeightSlider);
        this.rotationSlider.copy(rotationSlider);
        this.name.setValue(s);
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
                public void setFocused(boolean focused) {
                    boolean wasFocused = this.isFocused();
                    super.setFocused(focused);
                    if (wasFocused && !focused) {
                        EyeOptionsScreen.this.changeProfile(this.getValue());
                    }
                }
            };
            this.name.setTextColor(-1);
            this.name.setTextColorUneditable(-12632257);
            this.name.setMaxLength(50);
            this.name.setHint(Component.literal(playerInfo.getProfile().name()));
            this.addRenderableWidget(this.name);
        }

        this.changeYPos.put(this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) ->
                        this.minecraft.gui.setScreen(this.parent)).bounds(this.width / 2 - 166, this.height / 2 + 75, 128, 20).build()),
                y -> y + (!laserOptions.isEmpty() ? laserOptions.size() > 3 ? 70 : 40 : 0) + 5);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return this.name.keyPressed(event) || this.name.canConsumeInput() || super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor GuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.playerInfo != null) {
            PlayerModelType currentModel = this.playerInfo.getSkin().model();
            if (this.skinModel != currentModel) {
                this.skinModel = currentModel;
                boolean slim = currentModel == PlayerModelType.SLIM;
                this.model = new PlayerModel(Minecraft.getInstance().getEntityModels().bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim);
            }
        }
        this.setModelProperties(this.model, pPartialTick);
        Matrix3x2fStack matrix = GuiGraphicsExtractor.pose();

        if (this.minecraft != null && this.minecraft.level == null) {
            this.extractPanorama(GuiGraphicsExtractor, pPartialTick);
        }

        this.extractMenuBackground(GuiGraphicsExtractor);

        int h = this.height / 2 + (laserOptions.isEmpty() ? 35 : laserOptions.size() < 4 ? 15 : 0);
        {
            float f = 2.5F;
            matrix.pushMatrix();
            matrix.scale(f, f);
            matrix.translate((this.width / 2F - 200) / f, (h - 95) / f);
            GuiGraphicsExtractor.text(this.font, Component.translatable("title.theboys").withStyle(ClientUtil.BOLD_MINECRAFT), 0, 0,
                    ARGB.color(255, 170, 20, 20), true);
            matrix.popMatrix();
        }

        GuiGraphicsExtractor.fill(this.width / 2 - 200, h - 70, this.lastXofPresets, h + (!laserOptions.isEmpty() ? laserOptions.size() > 3 ? 70 : 40 : 0), 1979711488);
        GuiGraphicsExtractor.fill(this.width / 2 - 200, h - 72, this.lastXofPresets, h - 70, 1191182335);

        int i = this.width / 2 + 80;
        int j = this.height / 2 - 70;
        GuiGraphicsExtractor.fill(i, j - 2, i + 100, j, 1191182335);
        GuiGraphicsExtractor.enableScissor(i, j, i + 100, j + 140);
        GuiGraphicsExtractor.fill(i, j, i + 100, j + 140, 1979711488);
        renderEntityInInventory(GuiGraphicsExtractor, i + 50, j + 130, 60, (float) (i + 50) - pMouseX, (float) (j + 51) - pMouseY);
        GuiGraphicsExtractor.disableScissor();
        this.changeYPos.forEach((key, value) -> {
            if (key instanceof AbstractWidget w) {
                w.setY(value.apply(h));
            }
            if (key instanceof ModLabel l) {
                l.setY(value.apply(h));
            }
        });
        super.extractRenderState(GuiGraphicsExtractor, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {

    }

    public void setModelProperties(PlayerModel model, float pPartialTick) {
        if (this.minecraft == null || model == null) return;
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

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == 0 && this.name != null) {
            boolean insideName = event.x() >= (double) this.name.getX() && event.x() < (double) (this.name.getX() + this.name.getWidth())
                    && event.y() >= (double) this.name.getY() && event.y() < (double) (this.name.getY() + this.name.getHeight());
            if (this.name.isFocused() != insideName) {
                this.name.setFocused(insideName);
            }
        }
        return super.mouseClicked(event, isDoubleClick);
    }

    public void renderEntityInInventory(GuiGraphicsExtractor GuiGraphicsExtractor, int x, int y, int scale, float angleXComponent, float angleYComponent) {
        if (this.playerInfo == null) {
            return;
        }

        PlayerModelType modelType = this.playerInfo.getSkin().model();
        if (this.model == null || this.skinModel != modelType) {
            boolean slim = modelType == PlayerModelType.SLIM;
            this.model = new PlayerModel(Minecraft.getInstance().getEntityModels().bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim);
            this.skinModel = modelType;
        }

        float pointerYaw = (float) (Math.atan(angleXComponent / 40.0F) * 40.0F);
        float rotationYaw = this.rotationSlider == null ? 0.0F : (float) this.rotationSlider.getValue();
        float pitch = 10F -(float) Math.atan(angleYComponent / 40.0F) * 20.0F;
        this.model.head.yRot = pointerYaw * ((float) Math.PI / 180F);
        this.model.head.xRot = pitch * ((float) Math.PI / 180F);

        Identifier texture = this.playerInfo.getSkin().body().texturePath();
        int width = 100;
        int height = 160;
        submitLaserOverlay(GuiGraphicsExtractor, texture, x, y + 10, width, height, scale, -pitch, rotationYaw);
    }

    private void submitLaserOverlay(GuiGraphicsExtractor GuiGraphicsExtractor, Identifier texture, int x, int y, int width, int height, int scale, float rotationX, float rotationY) {
        if (this.model == null) return;
        int eyesHeight = this.isCustomEyesType() && this.eyesHeightSlider != null ? (int) this.eyesHeightSlider.getValue() : getEyesHeight();
        int eyesLength = this.isCustomEyesType() && this.eyesLengthSlider != null ? (int) this.eyesLengthSlider.getValue() : getEyesLength();
        LaserPreviewRenderState laserState = new LaserPreviewRenderState(
                this.model,
                texture,
                rotationX,
                rotationY,
                -1.5F,
                eyesHeight,
                eyesLength,
                LASERS_LENGTH,
                this.tickCount,
                x - width / 2,
                y - height,
                x + width / 2,
                y,
                scale,
                GuiGraphicsExtractor.scissorStack.peek()
        );
        GuiGraphicsExtractor.guiRenderState.addPicturesInPictureState(laserState);
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

    private boolean isCustomEyesType() {
        int type = TBConfig.CLIENT.eyesType.get();
        return type == 4 || type == 5;
    }

    private void changeProfile(String input) {
        if (this.minecraft == null) return;
        String trimmed = input.trim();
        if (StringUtil.isNullOrEmpty(trimmed)) {
            return;
        }
        if (this.playerInfo != null && trimmed.equalsIgnoreCase(this.playerInfo.getProfile().name())) {
            return;
        }
        ((ISetupGameProfiles) this.minecraft).theBoys$setup();

        final int requestId = this.profileRequestCounter.incrementAndGet();
        final String requestName = trimmed;
        Util.backgroundExecutor().execute(() -> {
            GameProfile profile = this.lookupProfileBlocking(requestName);
            this.minecraft.execute(() -> this.applyResolvedProfile(requestId, profile));
        });
    }

    private GameProfile lookupProfileBlocking(String playerName) {
        Minecraft minecraft = this.minecraft;
        if (minecraft == null) {
            return new GameProfile(generateOfflineUuid(playerName), playerName);
        }
        Services services = minecraft.services();
        GameProfile profile = null;
        try {
            profile = services.profileResolver().fetchByName(playerName).orElse(null);
        } catch (Exception exception) {
            TheBoys.LOGGER.warn("Profile lookup threw for {}", playerName, exception);
        }
        if (profile == null) {
            profile = new GameProfile(generateOfflineUuid(playerName), playerName);
        }
        return this.enrichProfile(services.sessionService(), profile, playerName);
    }

    private GameProfile enrichProfile(@Nullable MinecraftSessionService sessionService, GameProfile profile, String fallbackName) {
        GameProfile resolved = profile;
        if (sessionService != null && profile.id() != null) {
            try {
                ProfileResult result = sessionService.fetchProfile(profile.id(), true);
                if (result != null && result.profile() != null) {
                    resolved = result.profile();
                }
            } catch (Exception e) {
                TheBoys.LOGGER.warn("Failed to fetch profile data for {}", profile.name(), e);
            }
        }
        if (resolved.name() == null || resolved.name().isBlank()) {
            UUID id = resolved.id() != null ? resolved.id() : generateOfflineUuid(fallbackName);
            resolved = new GameProfile(id, fallbackName);
            resolved.properties().putAll(profile.properties());
        }
        return resolved;
    }

    private void applyResolvedProfile(int requestId, GameProfile profile) {
        if (this.minecraft == null || requestId != this.profileRequestCounter.get()) {
            return;
        }
        PlayerInfo playerinfo = new PlayerInfo(profile, false);
        playerinfo.getSkin(); // update textures and model
        this.minecraft.getPlayerSocialManager().addPlayer(playerinfo);
        this.playerInfo = playerinfo;
        if (this.name != null && profile.name() != null) {
            this.name.setHint(Component.literal(profile.name()));
        }
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
                screen.eyesHeightSlider.setRealValue(getEyesHeight());
                screen.eyesLengthSlider.setRealValue(getEyesLength());

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
                        screen.eyesHeightSlider.setRealValue(6);
                        screen.eyesLengthSlider.valueToInitial();
                    }
                    case 3 -> {
                        screen.eyesLengthSlider.setRealValue(2);
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
        public void extractContents(GuiGraphicsExtractor GuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTick) {
            this.setFocused(TBConfig.CLIENT.eyesType.get() == this.type);
            //super.extractContents(GuiGraphicsExtractor, pMouseX, pMouseY, pPartialTick);
            Identifier Identifier = this.sprites.get(this.isActive(), this.isHoveredOrFocused());
            GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, Identifier, this.getX(), this.getY(), 0, 24 + (this.isHoveredOrFocused() ? 32 : 0), 32, 32, 128, 128);
            GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, Identifier, this.getX() + 4, this.getY() + 4, type > 3 ? 72 : (type - 1) * 24, 0, 24, 24, 128, 128);
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
        public void extractRenderState(GuiGraphicsExtractor GuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTick) {
            GuiGraphicsExtractor.text(Minecraft.getInstance().font, this.text, this.x, this.y, this.color);
        }
    }
}