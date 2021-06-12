package chappie.theboys.abilities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayer;
import xyz.heroesunited.heroesunited.util.HUClientUtil;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;
import xyz.heroesunited.heroesunited.util.hudata.HUData;

import java.awt.*;
import java.util.Map;

public class StarLightAbility extends Ability {
    public static final HUData<Boolean> POWERS_ACTIVATED = new HUData("enabled");
    public static final HUData<Integer> ENERGY_EATED = new HUData("energy_eated"), TICKS = new HUData("ticks");

    public StarLightAbility() {
        super(TBAbilityTypes.STARLIGHT);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(POWERS_ACTIVATED, false);
        this.dataManager.register(ENERGY_EATED, 0);
        this.dataManager.register(TICKS, 0);
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        super.onUpdate(player);
        if (this.dataManager.get(POWERS_ACTIVATED)) {
            this.dataManager.set(player, TICKS, this.dataManager.get(TICKS) + 1);
        } else this.dataManager.set(player, TICKS, 0);

        if (this.dataManager.get(TICKS) >= 1200 && this.dataManager.get(ENERGY_EATED) > 50) {
            this.dataManager.set(player, ENERGY_EATED, this.dataManager.get(ENERGY_EATED) - 50);
            this.dataManager.set(player, TICKS, this.dataManager.get(TICKS) - 1200);
        }

        if (this.dataManager.get(TICKS) >= 6000) {
            this.dataManager.set(player, POWERS_ACTIVATED, false);
        }

        if (this.dataManager.get(ENERGY_EATED) > 10 && !this.dataManager.get(POWERS_ACTIVATED)) {
            this.dataManager.set(player, POWERS_ACTIVATED, true);
            this.dataManager.set(player, ENERGY_EATED, this.dataManager.get(ENERGY_EATED) - 10);
        }
    }

    @Override
    public void onKeyInput(PlayerEntity player, Map<Integer, Boolean> map) {
        super.onKeyInput(player, map);
        if (map.get(5)) {
            BlockPos.betweenClosedStream(HUPlayerUtil.getCollisionBoxWithRange(HUPlayerUtil.getPlayerPos(player), 5)).forEach(pos -> {
                if (!player.level.isClientSide) {
                    BlockState state = player.level.getBlockState(pos);
                    BooleanProperty property = state.getBlock() instanceof RedstoneLampBlock ? RedstoneLampBlock.LIT : BlockStateProperties.POWERED;
                    IntegerProperty property1 = BlockStateProperties.POWER;
                    if (state.getOptionalValue(property1).isPresent() && state.getOptionalValue(property1).get() != 0) {
                        player.level.setBlock(pos, state.setValue(property1, 0), 2);
                        this.dataManager.set(player, ENERGY_EATED, this.dataManager.get(ENERGY_EATED) + 1);
                    }
                    if (state.getOptionalValue(property).isPresent() && state.getOptionalValue(property).get()) {
                        player.level.setBlock(pos, state.setValue(property, false), 2);
                        this.dataManager.set(player, ENERGY_EATED, this.dataManager.get(ENERGY_EATED) + 1);
                    }
                    HUPlayer.getCap(player).sync();
                }
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (this.dataManager.get(POWERS_ACTIVATED)) {
            for (HandSide side : HandSide.values()) {
                float r = 0.15F;
                float i = this.dataManager.get(TICKS) <= 1200 ? 1f : 0.5F;
                AxisAlignedBB box = new AxisAlignedBB(-r, -r, -r, r, r, r);
                matrix.pushPose();
                renderer.getModel().translateToHand(side, matrix);
                matrix.translate(side == HandSide.LEFT ? 0.06 : -0.06, 0.55, 0);
                HUClientUtil.renderAura(matrix, bufferIn.getBuffer(HUClientUtil.HURenderTypes.LASER), box, 0.025F, new Color(i, i, 0, i), packedLightIn, player.tickCount);
                matrix.popPose();
            }
        }
    }
}
