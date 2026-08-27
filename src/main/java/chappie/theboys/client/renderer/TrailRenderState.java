package chappie.theboys.client.renderer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import java.awt.*;
import java.util.Map;

public class TrailRenderState extends LivingEntityRenderState {
    public float yBodyRot;
    public LivingEntity attached;
    public int lifeTime;
    public Color color;
    public Map<String, Object> fieldSavingMap;
    public int tickCount;
    public double distanceToSqr;
    public float partialTick;
    public TrailResources trail;

    public record TrailResources(EntityModel<? extends EntityRenderState> model, Identifier texture) {
    }
}
