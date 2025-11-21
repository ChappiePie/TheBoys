package chappie.theboys.util;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.XRayAbility;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TranslucentBlocksUtil {

    public static final float NO_ALPHA = -1.0F;

    public static boolean canSeeThrough(BlockPos.MutableBlockPos pos) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && Minecraft.getInstance().getCameraEntity() != null) {
            try {
                for (XRayAbility xRayAbility : CommonUtil.listOfType(XRayAbility.class, CommonUtil.getAbilities(Minecraft.getInstance().getCameraEntity()))) {
                    if (xRayAbility.hitPos != null && xRayAbility.translucentTimer.value(ClientUtil.getPartialTick()) != 0) {
                        AABB aabb = new AABB(xRayAbility.blockHitPos).inflate(xRayAbility.dataManager.get(XRayAbility.DISTANCE_MULTIPLIER));
                        if (aabb.intersects(new AABB(pos))) {
                            return true;
                        }
                    }
                }
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }

    public static float resolveAlpha(BlockPos pos) {
        if (pos == null || FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            return NO_ALPHA;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getCameraEntity() == null) {
            return NO_ALPHA;
        }

        try {
            float partialTick = ClientUtil.getPartialTick();
            for (XRayAbility xRayAbility : CommonUtil.listOfType(XRayAbility.class, CommonUtil.getAbilities(minecraft.getCameraEntity()))) {
                if (xRayAbility.hitPos == null) {
                    continue;
                }

                float timer = xRayAbility.translucentTimer.value(partialTick);
                if (timer == 0) {
                    continue;
                }

                float distanceMul = xRayAbility.dataManager.get(XRayAbility.DISTANCE_MULTIPLIER);
                AABB effectBounds = new AABB(xRayAbility.blockHitPos).inflate(distanceMul);
                if (!effectBounds.intersects(new AABB(pos))) {
                    continue;
                }

                float alpha = (float) (xRayAbility.hitPos.distanceTo(Vec3.atCenterOf(pos)) / (distanceMul + 1));
                alpha = (1.0F - Math.max(0.5F, Math.min(alpha, 1.0F))) * timer;
                alpha = 1.0F - alpha;
                if (alpha < 1.0F) {
                    return alpha;
                }
            }
        } catch (Exception ignored) {
            return NO_ALPHA;
        }

        return NO_ALPHA;
    }
}
