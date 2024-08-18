package chappie.theboys.common.entity;

import chappie.modulus.mixin.client.EntityRenderersAccessor;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.render.IHasContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

import java.awt.*;
import java.util.Map;

import static net.minecraft.client.renderer.entity.LivingEntityRenderer.isEntityUpsideDown;

public class TrailEntity extends Entity implements IEntityAdditionalSpawnData {
    @OnlyIn(Dist.CLIENT)
    public EntityModel<LivingEntity> model;
    public ResourceLocation texture;
    public float yBodyRot;
    public LivingEntity entity;
    public int lifeTime;
    public Color color;
    public Map<String, Object> fieldSavingMap;

    public TrailEntity(EntityType<TrailEntity> entityType, Level world) {
        super(entityType, world);
        this.noPhysics = true;
        this.color = Color.RED;
        this.noCulling = true;
    }

    public TrailEntity(Level worldIn, LivingEntity entity, Color color, int lifeTime) {
        this(TBEntities.TRAIL.get(), worldIn);
        this.entity = entity;
        this.yBodyRot = entity.yBodyRot;
        this.lifeTime = lifeTime;
        this.color = color;
        this.setYRot(entity.getYRot());
        this.setXRot(entity.getXRot());
        this.moveTo(entity.position().add(Mth.sin(-entity.getYRot() * ((float)Math.PI / 180F)) * -0.25F, 0.0D, Mth.cos(entity.getYRot() * ((float)Math.PI / 180F)) * -0.25F));
    }

    @Override
    public EntityDimensions getDimensions(Pose poseIn) {
        if (this.entity != null) {
            return this.entity.getDimensions(poseIn);
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
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(this.lifeTime);
        buffer.writeInt(this.entity == null ? -1 : this.entity.getId());

        buffer.writeInt(this.color.getRed());
        buffer.writeInt(this.color.getGreen());
        buffer.writeInt(this.color.getBlue());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.lifeTime = additionalData.readInt();
        this.entity = (LivingEntity) this.getCommandSenderWorld().getEntity(additionalData.readInt());
        this.color = new Color(additionalData.readInt(), additionalData.readInt(), additionalData.readInt());

        if (this.entity == null) return;

        if (this.entity instanceof AbstractClientPlayer player) {
            PlayerModel<LivingEntity> model = new PlayerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(
                            CommonUtil.smallArms(player) ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), CommonUtil.smallArms(player));
            model.hat.visible = false;
            model.leftSleeve.visible = false;
            model.rightSleeve.visible = false;
            model.leftPants.visible = false;
            model.rightPants.visible = false;
            model.jacket.visible = false;
            this.model = model;
            this.texture = player.getSkin().texture();
        } else {
            if (EntityRenderersAccessor.providers().get(this.entity.getType()).create(IHasContext.getContext()) instanceof LivingEntityRenderer renderer) {
                this.model = (EntityModel<LivingEntity>) renderer.getModel();
                this.texture = renderer.getTextureLocation(this.entity);
            }
        }

        this.fieldSavingMap = Map.of("isFallFlying", this.entity.isFallFlying(),
                "fallFlyingTicks", this.entity.getFallFlyingTicks(),
                "xRot", this.entity.getXRot(),
                "yRot", this.entity.getYRot(),
                "swimAmount", this.entity.getSwimAmount(1),
                "deltaMovement", this.entity.getDeltaMovement(),
                "isInWater", this.entity.isInWater(),
                "isVisuallySwimming", this.entity.isVisuallySwimming());

        {
            this.yBodyRot = this.entity.yBodyRot;
            this.model.attackTime = this.entity.getAttackAnim(0);
            boolean shouldSit = this.entity.isPassenger() && (entity.getVehicle() != null && this.entity.getVehicle().shouldRiderSit());
            this.model.riding = shouldSit;
            this.model.young = this.entity.isBaby();
            float f = Mth.rotLerp(0, this.entity.yBodyRotO, this.entity.yBodyRot);
            float f1 = Mth.rotLerp(0, this.entity.yHeadRotO, this.entity.yHeadRot);
            float f2 = f1 - f;
            if (shouldSit && this.entity.getVehicle() instanceof LivingEntity livingentity) {
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

            float f6 = Mth.lerp(0, this.entity.xRotO, this.entity.getXRot());
            if (isEntityUpsideDown(entity)) {
                f6 *= -1.0F;
                f2 *= -1.0F;
            }

            float f8 = 0.0F;
            float f5 = 0.0F;
            if (!shouldSit && this.entity.isAlive()) {
                f8 = this.entity.walkAnimation.speed(1);
                f5 = this.entity.walkAnimation.position(1);
                if (this.entity.isBaby()) {
                    f5 *= 3.0F;
                }

                if (f8 > 1.0F) {
                    f8 = 1.0F;
                }
            }

            if (this.model instanceof HumanoidModel<?> model) {
                model.hat.visible = false;
                model.crouching = this.entity.isCrouching();
            }
            this.model.prepareMobModel(this.entity, f5, f8, 0);
            this.model.setupAnim(this.entity, f5, f8, this.entity.tickCount, f2, f6);
        }
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
