package chappie.theboys.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.awt.*;
import java.util.Map;

public class TrailRenderState extends LivingEntityRenderState {
    @Environment(EnvType.CLIENT)
    public EntityModel<? extends EntityRenderState> model;
    public ResourceLocation texture;
    public float yBodyRot;
    public LivingEntity attached;
    public int lifeTime;
    public Color color;
    public Map<String, Object> fieldSavingMap;
    public int tickCount;
    public double distanceToSqr;
    public float partialTick;
}
