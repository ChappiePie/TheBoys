package chappie.theboys.common.entity;

import chappie.modulus.networking.ModNetworking;
import chappie.theboys.networking.client.ClientSpawnTrail;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;

import java.awt.*;
import java.util.Map;

public class TrailEntity extends Entity {
    @Environment(EnvType.CLIENT)
    public EntityModel<LivingEntity> model;
    public ResourceLocation texture;
    public float yBodyRot;
    public LivingEntity attached;
    public int lifeTime;
    public Color color;
    public Map<String, Object> fieldSavingMap;

    public TrailEntity(EntityType<TrailEntity> entityType, Level world) {
        super(entityType, world);
        this.noPhysics = true;
        this.color = Color.RED;
        this.noCulling = true;
    }

    public TrailEntity(Level worldIn, LivingEntity attached, Color color, int lifeTime) {
        this(TBEntities.TRAIL, worldIn);
        this.attached = attached;
        this.yBodyRot = attached.yBodyRot;
        this.lifeTime = lifeTime;
        this.color = color;
        this.setYRot(attached.getYRot());
        this.setXRot(attached.getXRot());
        this.moveTo(attached.position().add(Mth.sin(-attached.getYRot() * ((float) Math.PI / 180F)) * -0.25F, 0.0D, Mth.cos(attached.getYRot() * ((float) Math.PI / 180F)) * -0.25F));
    }

    public static void startTracking(Entity entity, ServerPlayer serverPlayer) {
        if (entity instanceof TrailEntity e) {
            ModNetworking.send(new ClientSpawnTrail(e), serverPlayer);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount >= this.lifeTime) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    public EntityDimensions getDimensions(Pose poseIn) {
        if (this.attached != null) {
            return this.attached.getDimensions(poseIn);
        }
        return super.getDimensions(poseIn);
    }
}