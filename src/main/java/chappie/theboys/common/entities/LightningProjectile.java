package chappie.theboys.common.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.awt.*;

public class LightningProjectile extends ThrowableProjectile implements IEntityAdditionalSpawnData {

    private Type lightningType;
    private int damage;
    private Color color;
    private float gravity;
    private int lifetime;

    public LightningProjectile(LivingEntity entity, Type lightningType, int damage, Color color, int lifetime) {
        this(TBEntities.LIGHTNING_PROJECTILE.get(), entity.level);
        this.lightningType = lightningType;
        this.damage = damage;
        this.color = color;
        this.lifetime = lifetime;
        this.setOwner(entity);
        this.moveTo(entity.getX(), (entity.getY() + entity.getEyeHeight()) - 0.25D, entity.getZ(), entity.getYRot(), entity.getXRot());
    }

    public LightningProjectile(EntityType<LightningProjectile> lightningType, Level world) {
        super(lightningType, world);
        this.lightningType = Type.LIGHTNING;
        this.damage = 4;
        this.color = Color.RED;
        this.gravity = 0;
        this.lifetime = 60;
    }

    public Type getLightningType() {
        return lightningType;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public void tick() {
        if (!this.level.isClientSide && tickCount > lifetime) {
            this.removeAfterChangingDimensions();
        }
        super.tick();
    }

    @Override
    protected void onHit(HitResult rtr) {
        if (rtr == null || !isAlive())
            return;

        if (rtr.getType() != HitResult.Type.MISS) {
            setFire(damage);
        }

        if (!this.level.isClientSide) this.removeAfterChangingDimensions();
    }

    @Override
    protected float getGravity() {
        return gravity;
    }

    public Color getColor() {
        return color;
    }

    private void setFire(int value) {
        Difficulty difficulty = this.level.getDifficulty();
        if (difficulty != Difficulty.PEACEFUL && !this.level.isClientSide && this.level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            BlockPos blockpos = this.blockPosition();
            BlockState blockstate = BaseFireBlock.getState(this.level, blockpos);
            if (this.level.getBlockState(blockpos).isAir() && blockstate.canSurvive(this.level, blockpos)) {
                this.level.setBlockAndUpdate(blockpos, blockstate);
            }

            for (int i = 0; i < value; ++i) {
                BlockPos blockpos1 = blockpos.offset(this.random.nextInt(3) - 1, this.random.nextInt(3) - 1, this.random.nextInt(3) - 1);
                blockstate = BaseFireBlock.getState(this.level, blockpos1);
                if (this.level.getBlockState(blockpos1).isAir() && blockstate.canSurvive(this.level, blockpos1)) {
                    this.level.setBlockAndUpdate(blockpos1, blockstate);
                }
            }

        }
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.AMBIENT, 0.5F, 0.5F + this.random.nextFloat() * 0.2F);
        //this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundCategory.WEATHER, 2.0F, 0.5F + this.random.nextFloat() * 0.2F);

    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public boolean isInWater() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Nonnull
    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("damage", this.damage);
        compound.putFloat("gravity", this.gravity);
        serialize(compound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.damage = compound.getInt("damage");
        this.gravity = compound.getFloat("gravity");
        deserialize(compound);
    }

    public void serialize(CompoundTag compound) {
        compound.putInt("lifetime", lifetime);
        ListTag listNBT = new ListTag();
        listNBT.add(IntTag.valueOf(this.color.getRed()));
        listNBT.add(IntTag.valueOf(this.color.getGreen()));
        listNBT.add(IntTag.valueOf(this.color.getBlue()));
        compound.put("color", listNBT);
        compound.putString("lightningType", lightningType.name);
    }

    private void deserialize(CompoundTag compound) {
        ListTag listNBT = compound.getList("color", Tag.TAG_INT);
        this.lifetime = compound.getInt("lifetime");
        this.color = new Color(listNBT.getInt(0), listNBT.getInt(1), listNBT.getInt(2));
        this.lightningType = Type.getByName(compound.getString("lightningType"));
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeNbt(this.serializeNBT());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.deserialize(additionalData.readNbt());
    }

    public enum Type {
        STARLIGHT("starlight"),
        LIGHTNING("lightning");

        public final String name;

        Type(String name) {
            this.name = name;
        }

        public static Type getByName(String name) {
            for (Type type : values()) {
                if (type.name.equals(name)) {
                    return type;
                }
            }
            return null;
        }
    }
}