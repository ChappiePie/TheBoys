package chappie.theboys.mixin.client.sodium;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.XRayAbility;
import chappie.theboys.util.TranslucentBlocksUtil;
import chappie.theboys.util.interfaces.IWithAlpha;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer")
public abstract class MixinBlockRenderer extends AbstractBlockRenderContext {

    @ModifyVariable(
            method = "bufferQuad",
            at = @At("HEAD"),
            index = 3,
            argsOnly = true
    )
    private Material theBoys$forceTranslucent(Material value) {
        if (TranslucentBlocksUtil.canSeeThrough(this.pos.mutable())) {
            return DefaultMaterials.TRANSLUCENT;
        }
        return value;
    }

    @WrapOperation(
            method = "bufferQuad",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/api/util/ColorARGB;toABGR(I)I"
            )
    )
    private int theBoys$fixAlpha(int argb, Operation<Integer> original) {
        int abgr = original.call(argb);
        int a = (abgr >>> 24) & 0xFF;
        BlockPos pos = this.pos;
        if (Minecraft.getInstance().getCameraEntity() != null) {
            try {
                for (XRayAbility xRayAbility : CommonUtil.listOfType(XRayAbility.class, CommonUtil.getAbilities(Minecraft.getInstance().getCameraEntity()))) {
                    float t = xRayAbility.translucentTimer.value(ClientUtil.getPartialTick());
                    if (t != 0) {
                        Vec3 vec = xRayAbility.hitPos;
                        if (vec != null) {
                            float distantMul = xRayAbility.dataManager.get(XRayAbility.DISTANCE_MULTIPLIER);
                            AABB aabb = new AABB(xRayAbility.blockHitPos).inflate(distantMul);
                            if (aabb.intersects(new AABB(pos))) {
                                float alpha = (float) (vec.distanceTo(Vec3.atCenterOf(pos)) / (distantMul + 1));
                                alpha = (1f - Math.max(0.5F, Math.min(alpha, 1F))) * t;
                                alpha = 1.0F - alpha;
                                if (alpha != 1 && pos instanceof IWithAlpha i) {
                                    a = (int) (alpha * 255);
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (ArrayIndexOutOfBoundsException | NullPointerException ignored) {
                // Race condition in concurrent chunk rendering - abilities may be modified while iterating
            }
        }
        if (a < 0) a = 0; else if (a > 255) a = 255;
        return (a << 24) | (abgr & 0x00FFFFFF);
    }
}