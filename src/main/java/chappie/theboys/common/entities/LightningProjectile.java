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
    protected void registerData() {}

    @Override
    protected void onEntityHit(EntityRayTraceResult rtr) {
        if (!this.world.isRemote) {
            LightningBoltEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
            bolt.setPosition(rtr.getEntity().getPosX(), rtr.getEntity().getPosY(), rtr.getEntity().getPosZ());
            this.world.addEntity(bolt);
        }
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected float getGravityVelocity() {
        return 0.01F;
    }
}