package chappie.theboys.util;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.XRayAbility;
import chappie.theboys.util.interfaces.IWithAlpha;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TranslucentBlocksUtil {

    public static boolean canSeeThrough(BlockPos.MutableBlockPos pos) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && Minecraft.getInstance().getCameraEntity() != null) {
            for (XRayAbility xRayAbility : CommonUtil.listOfType(XRayAbility.class, CommonUtil.getAbilities(Minecraft.getInstance().getCameraEntity()))) {
                if (xRayAbility.hitPos != null && xRayAbility.translucentTimer.value(ClientUtil.getPartialTick()) != 0) {
                    AABB aabb = new AABB(BlockPos.containing(xRayAbility.hitPos)).inflate(xRayAbility.dataManager.get(XRayAbility.DISTANCE_MULTIPLIER));
                    if (aabb.intersects(new AABB(pos))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean blockCompile(BlockRenderDispatcher instance, BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack poseStack, VertexConsumer consumer) {
        for (XRayAbility xRayAbility : CommonUtil.listOfType(XRayAbility.class, CommonUtil.getAbilities(Minecraft.getInstance().getCameraEntity()))) {
            float t = xRayAbility.translucentTimer.value(ClientUtil.getPartialTick());
            if (t != 0) {
                Vec3 vec = xRayAbility.hitPos;
                if (vec != null) {
                    float distantMul = xRayAbility.dataManager.get(XRayAbility.DISTANCE_MULTIPLIER);
                    AABB aabb = new AABB(BlockPos.containing(vec)).inflate(distantMul);
                    if (aabb.intersects(new AABB(pos))) {
                        float alpha = (float) (vec.distanceTo(Vec3.atCenterOf(pos)) / (distantMul + 1));
                        alpha = (1f - Math.max(0.5F, Math.min(alpha, 1F))) * t;
                        alpha = 1.0F - alpha;
                        if (alpha != 1) {
                            RandomSource random = RandomSource.create();
                            if (Minecraft.getInstance().getBlockRenderer().getModelRenderer() instanceof IWithAlpha i) {
                                i.theBoys$setAlpha(alpha);
                                Minecraft.getInstance().getBlockRenderer().getModelRenderer()
                                        .tesselateBlock(level, instance.getBlockModel(state), state, pos, poseStack, consumer, true, random, state.getSeed(pos), OverlayTexture.NO_OVERLAY);
                                i.theBoys$setAlpha(-1.0F);
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
