package chappie.theboys.abilities;

import chappie.theboys.util.TBUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.heroesunited.heroesunited.common.abilities.JSONAbility;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;

public class LasersFromEyesAbility extends JSONAbility {

    public LasersFromEyesAbility() {
        super(TBAbilityTypes.LASERS_FROM_EYES);
    }

    @Override
    public void action(PlayerEntity player) {
        super.action(player);
        if (this.enabled) {
            HUPlayerUtil.makeLaserLooking(player, 40D);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (this.enabled) {
            TBUtil.renderHeatvision(renderer, matrix, bufferIn, packedLightIn, player, true, HUJsonUtils.getColor(this.getJsonObject()));
            TBUtil.renderHeatvision(renderer, matrix, bufferIn, packedLightIn, player, false, HUJsonUtils.getColor(this.getJsonObject()));
        }
    }
}
