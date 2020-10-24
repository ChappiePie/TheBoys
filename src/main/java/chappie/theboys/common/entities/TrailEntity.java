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

public class TrailEntity extends Entity implements IEntityAdditionalSpawnData {

    public PlayerEntity parent;
    public int lifeTime;
    public float renderYawOffset, red = 1, green = 1, blue = 1;

    @OnlyIn(Dist.CLIENT)
    public PlayerRenderer renderer;

    public TrailEntity(World worldIn) {
        super(TBEntities.TRAIL, worldIn);
        this.noClip = true;
        this.ignoreFrustumCheck = true;
    }

    public TrailEntity(EntityType<TrailEntity> entityType, World world) {
        super(entityType, world);
        this.noClip = true;
        this.ignoreFrustumCheck = true;
    }

    public TrailEntity(World worldIn, PlayerEntity parent, int lifeTime) {
        super(TBEntities.TRAIL, worldIn);
        this.noClip = true;
        this.ignoreFrustumCheck = true;
        this.parent = parent;
        this.lifeTime = lifeTime;
        this.setLocationAndAngles(parent.getPosX(), parent.getPosY(), parent.getPosZ(), parent.rotationYaw, parent.rotationPitch);
    }

    @Override
    public EntitySize getSize(Pose poseIn) {
        return parent.getSize(poseIn);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.ticksExisted >= this.lifeTime) {
            this.remove();
        }
    }

    @Override
    protected void registerData() {

    }

    @Override
    protected void readAdditional(CompoundNBT compound) {

    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {

    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(this.lifeTime);
        buffer.writeInt(this.parent.getEntityId());
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        this.lifeTime = additionalData.readInt();

        Entity en = this.world.getEntityByID(additionalData.readInt());
        if (en instanceof PlayerEntity) this.parent = (PlayerEntity) en;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }
}
