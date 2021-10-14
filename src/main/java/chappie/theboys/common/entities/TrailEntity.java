package chappie.theboys.common.entities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

import java.awt.*;

public class TrailEntity extends Entity implements IEntityAdditionalSpawnData {
    @OnlyIn(Dist.CLIENT)
    public final BipedModel<AbstractClientPlayerEntity> model = new BipedModel<>(0);
    public float yBodyRot;
    public PlayerEntity player;
    public int lifeTime;
    public Color color;

    public TrailEntity(EntityType<TrailEntity> entityType, World world) {
        super(entityType, world);
        this.noPhysics = true;
        this.color = Color.RED;
        this.noCulling = true;
    }

    public TrailEntity(World worldIn, PlayerEntity player, Color color, int lifeTime) {
        super(TBEntities.TRAIL, worldIn);
        this.noPhysics = true;
        this.noCulling = true;
        this.player = player;
        this.yBodyRot = player.yBodyRot;
        this.color = color;
        this.lifeTime = lifeTime;
        this.moveTo(player.getX(), player.getY(), player.getZ(), player.yRot, player.xRot);
    }

    @Override
    public EntitySize getDimensions(Pose poseIn) {
        if (player != null) {
            return player.getDimensions(poseIn);
        }
        return super.getDimensions(poseIn);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount >= this.lifeTime) {
            this.remove();
        }
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(this.lifeTime);
        buffer.writeUUID(this.player.getUUID());

        buffer.writeInt(this.color.getRed());
        buffer.writeInt(this.color.getGreen());
        buffer.writeInt(this.color.getBlue());
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        this.lifeTime = additionalData.readInt();
        this.player = this.level.getPlayerByUUID(additionalData.readUUID());

        this.yBodyRot = player.yBodyRot;

        if (player instanceof AbstractClientPlayerEntity) {
            ((PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer((AbstractClientPlayerEntity) player)).getModel().copyPropertiesTo(this.model);
        }

        this.color = new Color(additionalData.readInt(), additionalData.readInt(), additionalData.readInt());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    protected void defineSynchedData() {}
    protected void readAdditionalSaveData(CompoundNBT compound) {}
    protected void addAdditionalSaveData(CompoundNBT compound) {}
}
