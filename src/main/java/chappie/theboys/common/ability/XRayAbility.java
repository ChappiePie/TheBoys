package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.data.DataAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class XRayAbility extends Ability {

    public static final DataAccessor<Float> DISTANCE_MULTIPLIER = new DataAccessor<>("distance_multiplier", DataAccessor.DataSerializer.FLOAT);
    public IHasTimer.Timer translucentTimer = new IHasTimer.Timer(() -> 5, this::isEnabled);
    public Vec3 hitPos = null;
    public BlockPos blockHitPos = null;
    private AABB lastAABB = null;

    public XRayAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(DISTANCE_MULTIPLIER, 1F);
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        this.translucentTimer.update();
        float distantMul = this.dataManager.get(XRayAbility.DISTANCE_MULTIPLIER);
        if (this.entity.level().isClientSide() && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            if (this.hitPos != null) {
                AABB aabb = new AABB(hitPos, hitPos).inflate(distantMul);
                int minX = (int) Math.floor(aabb.minX);
                int minY = (int) Math.floor(aabb.minY);
                int minZ = (int) Math.floor(aabb.minZ);
                int maxX = (int) Math.floor(aabb.maxX);
                int maxY = (int) Math.floor(aabb.maxY);
                int maxZ = (int) Math.floor(aabb.maxZ);

                if (lastAABB == null || (int) lastAABB.minX != minX || (int) lastAABB.minY != minY || (int) lastAABB.minZ != minZ ||
                        (int) lastAABB.maxX != maxX || (int) lastAABB.maxY != maxY || (int) lastAABB.maxZ != maxZ) {

                    if (lastAABB != null) {
                        Minecraft.getInstance().levelRenderer.setBlocksDirty((int) lastAABB.minX, (int) lastAABB.minY, (int) lastAABB.minZ, (int) lastAABB.maxX, (int) lastAABB.maxY, (int) lastAABB.maxZ);
                    }
                    Minecraft.getInstance().levelRenderer.setBlocksDirty(minX, minY, minZ, maxX, maxY, maxZ);
                    lastAABB = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
                }
            } else if (lastAABB != null) {
                Minecraft.getInstance().levelRenderer.setBlocksDirty((int) lastAABB.minX, (int) lastAABB.minY, (int) lastAABB.minZ, (int) lastAABB.maxX, (int) lastAABB.maxY, (int) lastAABB.maxZ);
                lastAABB = null;
            }
        }
        double distance = 10;
        try {
            distance = this.entity.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        } catch (Throwable ignored) {
        }
        var hitResult = CommonUtil.pick(this.entity, distance * 3);
        if (hitResult instanceof BlockHitResult result) {
            this.hitPos = result.getLocation();
            this.blockHitPos = result.getBlockPos();
        } else {
            this.hitPos = null;
        }
    }
}