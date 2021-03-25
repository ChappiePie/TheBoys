package chappie.theboys.common.entities;

import net.minecraft.client.renderer.entity.PlayerRenderer;
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
    public PlayerRenderer renderer;
    public PlayerEntity player;
    public int lifeTime;
    public Color color;

    public TrailEntity(EntityType<TrailEntity> entityType, World world) {
        super(entityType, world);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public TrailEntity(World worldIn, PlayerEntity player, int lifeTime) {
        super(TBEntities.TRAIL, worldIn);
        this.noPhysics = true;
        this.noCulling = true;
        this.player = player;
        this.lifeTime = lifeTime;
        this.moveTo(player.getX(), player.getY(), player.getZ(), player.yRot, player.xRot);
    }

    @Override
    public EntitySize getDimensions(Pose poseIn) {
        return player.getDimensions(poseIn);
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
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        this.lifeTime = additionalData.readInt();
        this.player = this.level.getPlayerByUUID(additionalData.readUUID());
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
