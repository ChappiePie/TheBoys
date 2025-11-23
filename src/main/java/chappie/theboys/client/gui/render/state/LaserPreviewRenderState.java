package chappie.theboys.client.gui.render.state;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record LaserPreviewRenderState(
        PlayerModel model,
        ResourceLocation texture,
        float rotationX,
        float rotationY,
        float pivotY,
        float eyesHeight,
        float eyesLength,
        double laserLength,
        int tickCount,
        int x0,
        int y0,
        int x1,
        int y1,
        float scale,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {

    public LaserPreviewRenderState(
            PlayerModel model,
            ResourceLocation texture,
            float rotationX,
            float rotationY,
            float pivotY,
            float eyesHeight,
            float eyesLength,
            double laserLength,
            int tickCount,
            int x0,
            int y0,
            int x1,
            int y1,
            float scale,
            @Nullable ScreenRectangle scissorArea
    ) {
        this(
                model,
                texture,
                rotationX,
                rotationY,
                pivotY,
                eyesHeight,
                eyesLength,
                laserLength,
                tickCount,
                x0,
                y0,
                x1,
                y1,
                scale,
                scissorArea,
                PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea)
        );
    }
}
