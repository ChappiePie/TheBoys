package chappie.theboys.abilities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
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

import java.awt.*;

public class StarLightAbility extends Ability {
    private boolean powersActivated;
    private int blocksWithEnergyEated, ticksExisted;

    public StarLightAbility() {
        super(TBAbilityTypes.STARLIGHT);
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        super.onUpdate(player);
        if (powersActivated) {
            this.ticksExisted++;
        } else ticksExisted = 0;

        if (this.ticksExisted >= 1200 && this.blocksWithEnergyEated > 50) {
            this.blocksWithEnergyEated = this.blocksWithEnergyEated - 50;
            this.ticksExisted = this.ticksExisted - 1200;
        }

        if (this.ticksExisted >= 6000) {
            this.powersActivated = false;
        }

        if (this.blocksWithEnergyEated > 10 && !this.powersActivated) {
            this.powersActivated = true;
            this.blocksWithEnergyEated = this.blocksWithEnergyEated - 10;
            HUPlayer.getCap(player).sync();
        }
    }

    @Override
    public void toggle(PlayerEntity player, int id, boolean pressed) {
        super.toggle(player, id, pressed);
        switch (id) {
            case 5:
                if (pressed) {
                    BlockPos.betweenClosedStream(HUPlayerUtil.getCollisionBoxWithRange(HUPlayerUtil.getPlayerPos(player), 5)).forEach(pos -> {
                        if (!player.level.isClientSide) {
                            BlockState state = player.level.getBlockState(pos);
                            BooleanProperty property = state.getBlock() instanceof RedstoneLampBlock ? RedstoneLampBlock.LIT : BlockStateProperties.POWERED;
                            IntegerProperty property1 = BlockStateProperties.POWER;
                            if (state.getOptionalValue(property1).isPresent() && state.getOptionalValue(property1).get() != 0) {
                                player.level.setBlock(pos, state.setValue(property1, 0), 2);
                                blocksWithEnergyEated++;
                            }
                            if (state.getOptionalValue(property).isPresent() && state.getOptionalValue(property).get()) {
                                player.level.setBlock(pos, state.setValue(property, false), 2);
                                blocksWithEnergyEated++;
                            }
                            HUPlayer.getCap(player).sync();
                        }
                    });
                }
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putBoolean("powersActivated", this.powersActivated);
        nbt.putInt("blocksWithEnergyEated", this.blocksWithEnergyEated);
        nbt.putInt("ticksExisted", this.ticksExisted);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.powersActivated = nbt.getBoolean("powersActivated");
        this.blocksWithEnergyEated = nbt.getInt("blocksWithEnergyEated");
        this.ticksExisted = nbt.getInt("ticksExisted");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (powersActivated) {
            for (HandSide side : HandSide.values()) {
                float r = 0.15F;
                float i = this.ticksExisted <= 1200 ? 1f : 0.5F;
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
