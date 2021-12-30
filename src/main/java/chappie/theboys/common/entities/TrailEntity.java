package chappie.theboys.common.entities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import java.awt.*;

public class TrailEntity extends Entity implements IEntityAdditionalSpawnData {
    @OnlyIn(Dist.CLIENT)
    public final HumanoidModel<AbstractClientPlayer> model = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
    public float yBodyRot;
    public Player player;
    public int lifeTime;
    public Color color;

    public TrailEntity(EntityType<TrailEntity> entityType, Level world) {
        super(entityType, world);
        this.noPhysics = true;
        this.color = Color.RED;
        this.noCulling = true;
    }

    public TrailEntity(Level worldIn, Player player, Color color, int lifeTime) {
        super(TBEntities.TRAIL, worldIn);
        this.noPhysics = true;
        this.noCulling = true;
        this.player = player;
        this.yBodyRot = player.yBodyRot;
        this.color = color;
        this.lifeTime = lifeTime;
        this.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
    }

    @Override
    public EntityDimensions getDimensions(Pose poseIn) {
        if (player != null) {
            return player.getDimensions(poseIn);
        }
        return super.getDimensions(poseIn);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount >= this.lifeTime) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(this.lifeTime);
        buffer.writeUUID(this.player.getUUID());

        buffer.writeInt(this.color.getRed());
        buffer.writeInt(this.color.getGreen());
        buffer.writeInt(this.color.getBlue());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.lifeTime = additionalData.readInt();
        this.player = this.level.getPlayerByUUID(additionalData.readUUID());

        this.yBodyRot = player.yBodyRot;

        if (player instanceof AbstractClientPlayer) {
            ((PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer((AbstractClientPlayer) player)).getModel().copyPropertiesTo(this.model);
        }

        this.color = new Color(additionalData.readInt(), additionalData.readInt(), additionalData.readInt());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    protected void defineSynchedData() {}
    protected void readAdditionalSaveData(CompoundTag compound) {}
    protected void addAdditionalSaveData(CompoundTag compound) {}
}
