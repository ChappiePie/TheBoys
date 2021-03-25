package chappie.theboys.common.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.awt.*;

public class LightningProjectile extends ThrowableEntity {

    private Color color = Color.WHITE;

    public LightningProjectile(World world, Color color) {
        super(TBEntities.LIGHTNING_PROJECTILE, world);
        this.color = color;
    }

    public LightningProjectile(EntityType<LightningProjectile> type, World world) {
        super(type, world);
    }

    public Color getColor() {
        return color;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void onHitEntity(EntityRayTraceResult rtr) {
        if (!this.level.isClientSide) {
            LightningBoltEntity bolt = EntityType.LIGHTNING_BOLT.create(level);
            bolt.setPos(rtr.getEntity().getX(), rtr.getEntity().getY(), rtr.getEntity().getZ());
            this.level.addFreshEntity(bolt);
        }
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected float getGravity() {
        return 0.01F;
    }
}