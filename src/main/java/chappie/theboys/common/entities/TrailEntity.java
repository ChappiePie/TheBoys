package chappie.theboys.common.entities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import java.awt.*;
import java.util.Random;
import java.util.UUID;

import static net.minecraft.client.renderer.entity.LivingEntityRenderer.isEntityUpsideDown;

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
        this.setYRot(player.getYRot());
        this.setXRot(player.getXRot());
        this.moveTo(player.position().add(Mth.sin(-player.getYRot() * ((float)Math.PI / 180F)) * -0.5F, 0.0D, Mth.cos(player.getYRot() * ((float)Math.PI / 180F)) * -0.5F));
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

    public Random getRandom() {
        return this.random;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(this.lifeTime);
        buffer.writeUUID(this.player == null ? UUID.randomUUID() : player.getUUID());

        buffer.writeInt(this.color.getRed());
        buffer.writeInt(this.color.getGreen());
        buffer.writeInt(this.color.getBlue());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.lifeTime = additionalData.readInt();
        this.player = this.level.getPlayerByUUID(additionalData.readUUID());

        if (player instanceof AbstractClientPlayer player) {
            this.yBodyRot = this.player.yBodyRot;
            this.model.attackTime = player.getAttackAnim(0);
            boolean shouldSit = player.isPassenger() && (player.getVehicle() != null && player.getVehicle().shouldRiderSit());
            this.model.riding = shouldSit;
            this.model.young = player.isBaby();
            float f = Mth.rotLerp(0, player.yBodyRotO, player.yBodyRot);
            float f1 = Mth.rotLerp(0, player.yHeadRotO, player.yHeadRot);
            float f2 = f1 - f;
            if (shouldSit && player.getVehicle() instanceof LivingEntity livingentity) {
                f = Mth.rotLerp(0, livingentity.yBodyRotO, livingentity.yBodyRot);
                f2 = f1 - f;
                float f3 = Mth.wrapDegrees(f2);
                if (f3 < -85.0F) {
                    f3 = -85.0F;
                }

                if (f3 >= 85.0F) {
                    f3 = 85.0F;
                }

                f = f1 - f3;
                if (f3 * f3 > 2500.0F) {
                    f += f3 * 0.2F;
                }

                f2 = f1 - f;
            }

            float f6 = Mth.lerp(0, player.xRotO, player.getXRot());
            if (isEntityUpsideDown(player)) {
                f6 *= -1.0F;
                f2 *= -1.0F;
            }

            float f8 = 0.0F;
            float f5 = 0.0F;
            if (!shouldSit && player.isAlive()) {
                f8 = Mth.lerp(0, player.animationSpeedOld, player.animationSpeed);
                f5 = player.animationPosition - player.animationSpeed;
                if (player.isBaby()) {
                    f5 *= 3.0F;
                }

                if (f8 > 1.0F) {
                    f8 = 1.0F;
                }
            }

            this.model.crouching = player.isCrouching();
            this.model.prepareMobModel(player, f5, f8, 0);
            this.model.setupAnim(player, f5, f8, player.tickCount, f2, f6);
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
