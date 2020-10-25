package chappie.theboys.util;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.AbstractGlassBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.SandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;

public class TBUtil {

    public static void makeLaserLooking(PlayerEntity player) {
        RayTraceResult rtr = getPosLookingAt(player);
        if (rtr != null && !player.world.isRemote) {
            if (rtr.getType() == RayTraceResult.Type.ENTITY) {
                EntityRayTraceResult ertr = (EntityRayTraceResult) rtr;
                if (ertr.getEntity() != null && ertr.getEntity() != player) {
                    ertr.getEntity().setFire(5);
                    if (ertr.getEntity() instanceof PlayerEntity)
                        ertr.getEntity().attackEntityFrom(DamageSource.causePlayerDamage(player), 3);
                    else ertr.getEntity().attackEntityFrom(DamageSource.causeMobDamage(player), 3);
                }
            } else if (rtr.getType() == RayTraceResult.Type.BLOCK) {
                BlockPos pos = new BlockPos(rtr.getHitVec());
                for (Direction dir : Direction.values()) {
                    BlockPos blockPos = new BlockPos(pos.add(dir.getDirectionVec()));
                    if (player.world.isAirBlock(blockPos)) {
                        player.world.setBlockState(blockPos, Blocks.FIRE.getDefaultState());
                    }
                }
            }
        }
    }


    public static RayTraceResult getPosLookingAt(PlayerEntity player) {
        double distance = 40D;
        Vector3d startPos = player.getPositionVec().add(0, player.getEyeHeight(), 0);
        Vector3d endPos = player.getPositionVec().add(0, player.getEyeHeight(), 0).add(player.getLookVec().scale(distance));

        for (int i = 0; i < distance * 2; i++) {
            float scale = i / 2F;
            Vector3d pos = startPos.add(endPos.subtract(startPos).scale(scale / distance));
            BlockPos bpos = new BlockPos(pos);
            boolean block = !player.world.getBlockState(bpos).isSolid() && player.world.getBlockState(bpos).getBlock() instanceof AbstractGlassBlock;
            if ((player.world.getBlockState(bpos).isSolid() && !player.world.isAirBlock(bpos)) || block) {
                return new BlockRayTraceResult(pos, null, bpos, false);
            } else {
                Vector3d min = pos.add(0.25F, 0.25F, 0.25F);
                Vector3d max = pos.add(-0.25F, -0.25F, -0.25F);
                for (Entity entity : player.world.getEntitiesWithinAABBExcludingEntity(player, new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z))) {
                    return new EntityRayTraceResult(entity);
                }
            }
        }
        return null;
    }
}
