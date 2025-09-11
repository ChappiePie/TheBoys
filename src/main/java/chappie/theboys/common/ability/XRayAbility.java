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
        if (this.entity.level().isClientSide() && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && this.hitPos != null) {
            AABB aabb = new AABB(hitPos, hitPos).inflate(distantMul);
            Minecraft.getInstance().levelRenderer.setBlocksDirty((int) aabb.minX, (int) aabb.minY, (int) aabb.minZ, (int) aabb.maxX, (int) aabb.maxY, (int) aabb.maxZ);
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