package chappie.theboys.common.entities;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.awt.*;

public class LightningProjectile extends ThrowableEntity implements IEntityAdditionalSpawnData {

    private Type lightningType;
    private int damage;
    private Color color;
    private float gravity;
    private int lifetime;

    public LightningProjectile(World world, Type lightningType, int damage, Color color, int lifetime) {
        super(TBEntities.LIGHTNING_PROJECTILE, world);
        this.lightningType = lightningType;
        this.damage = damage;
        this.color = color;
        this.lifetime = lifetime;
    }

    public LightningProjectile(EntityType<LightningProjectile> lightningType, World world) {
        super(lightningType, world);
        /*this.type = Type.LIGHTNING;
        this.damage = 4;
        this.color = Color.RED;
        this.gravity = 0;
        this.lifetime = 60;*/
    }

    public Type getLightningType() {
        return lightningType;
    }

    @Override
    public void tick() {
        if (!this.level.isClientSide && tickCount > lifetime) {
            this.removeAfterChangingDimensions();
        }
        super.tick();
    }

    @Override
    protected void onHit(RayTraceResult rtr) {
        if (rtr == null || !isAlive())
            return;

        if (rtr.getType() != RayTraceResult.Type.MISS) {
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
            BlockState blockstate = AbstractFireBlock.getState(this.level, blockpos);
            if (this.level.getBlockState(blockpos).isAir() && blockstate.canSurvive(this.level, blockpos)) {
                this.level.setBlockAndUpdate(blockpos, blockstate);
            }

            for (int i = 0; i < value; ++i) {
                BlockPos blockpos1 = blockpos.offset(this.random.nextInt(3) - 1, this.random.nextInt(3) - 1, this.random.nextInt(3) - 1);
                blockstate = AbstractFireBlock.getState(this.level, blockpos1);
                if (this.level.getBlockState(blockpos1).isAir() && blockstate.canSurvive(this.level, blockpos1)) {
                    this.level.setBlockAndUpdate(blockpos1, blockstate);
                }
            }

        }
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_HURT_ON_FIRE, SoundCategory.AMBIENT, 10000.0F, 0.5F + this.random.nextFloat() * 0.2F);
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
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("damage", this.damage);
        compound.putFloat("gravity", this.gravity);
        serialize(compound);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        this.damage = compound.getInt("damage");
        this.gravity = compound.getFloat("gravity");
        deserialize(compound);
    }

    public void serialize(CompoundNBT compound) {
        compound.putInt("lifetime", lifetime);
        ListNBT listNBT = new ListNBT();
        listNBT.add(IntNBT.valueOf(this.color.getRed()));
        listNBT.add(IntNBT.valueOf(this.color.getGreen()));
        listNBT.add(IntNBT.valueOf(this.color.getBlue()));
        compound.put("color", listNBT);
        compound.putString("lightningType", lightningType.name);
    }

    private void deserialize(CompoundNBT compound) {
        ListNBT listNBT = compound.getList("color", Constants.NBT.TAG_INT);
        this.lifetime = compound.getInt("lifetime");
        this.color = new Color(listNBT.getInt(0), listNBT.getInt(1), listNBT.getInt(2));
        this.lightningType = Type.getByName(compound.getString("lightningType"));
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeNbt(this.serializeNBT());
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
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