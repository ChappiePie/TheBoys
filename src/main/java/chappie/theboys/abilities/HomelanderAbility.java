package chappie.theboys.abilities;

import chappie.theboys.TheBoys;
import chappie.theboys.util.TBClientUtil;
import chappie.theboys.util.TBUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import xyz.heroesunited.generatorrex.abilities.AbilityHelper;
import xyz.heroesunited.generatorrex.common.capability.IRex;
import xyz.heroesunited.generatorrex.common.capability.RexCap;
import xyz.heroesunited.generatorrex.network.GRType;
import xyz.heroesunited.generatorrex.network.Networking;
import xyz.heroesunited.generatorrex.network.server.SSetGRIntMessage;
import xyz.heroesunited.generatorrex.util.GRAttributes;
import xyz.heroesunited.generatorrex.util.GRClientUtil;
import xyz.heroesunited.generatorrex.util.GRPlayerUtil;
import xyz.heroesunited.heroesunited.client.events.HUSetRotationAnglesEvent;

import java.util.UUID;

public class HomelanderAbility extends TBAbility {

    private final UUID ATTRIBUTE_UUID = UUID.fromString("831d9761-3e36-46a6-ba4b-7a2e38d80d3b");

    public void onActivated(PlayerEntity player) {
        super.onActivated(player);
        AbilityHelper.addAttribute(player, Attributes.MAX_HEALTH, 10D, AttributeModifier.Operation.ADDITION, ATTRIBUTE_UUID);
        AbilityHelper.addAttribute(player, GRAttributes.JUMP_BOOST.get(), 2D, AttributeModifier.Operation.ADDITION, ATTRIBUTE_UUID);
        AbilityHelper.addAttribute(player, Attributes.ATTACK_DAMAGE, 2.0D, AttributeModifier.Operation.ADDITION, ATTRIBUTE_UUID);
        AbilityHelper.addAttribute(player, GRAttributes.FALL_RESISTANCE.get(), -1.0D, AttributeModifier.Operation.ADDITION, ATTRIBUTE_UUID);
    }

    public void onDeactivated(PlayerEntity player) {
        super.onDeactivated(player);
        AbilityHelper.removeAttribute(player, GRAttributes.FALL_RESISTANCE.get(), ATTRIBUTE_UUID);
        AbilityHelper.removeAttribute(player, Attributes.MAX_HEALTH, ATTRIBUTE_UUID);
        AbilityHelper.removeAttribute(player, GRAttributes.JUMP_BOOST.get(), ATTRIBUTE_UUID);
        AbilityHelper.removeAttribute(player, Attributes.ATTACK_DAMAGE, ATTRIBUTE_UUID);
    }

    public void onUpdate(PlayerEntity player) {
        IRex cap = RexCap.getCap(player);
        if (cap.isFlying() && player.getPosY() > 500.0D) {
            cap.setFlying(false);
        }
        if (player.world.isRemote && !AbilityHelper.keyPressed(1) && cap.getType() == 1) {
            Networking.INSTANCE.send(PacketDistributor.SERVER.noArg(), new SSetGRIntMessage(GRType.TYPE, 0));
        }
        if (cap.getType() == 1) {
            TBUtil.makeLaserLooking(player);
        }
    }

    public void toggle(PlayerEntity player, int id) {
        IRex cap = RexCap.getCap(player);
        if (id == 1) {
            if (cap.getType() == 0) {
                cap.setType(1);
            }
        } else if (id == 2) {
            if (cap.isFlying()) {
                cap.setFlying(false);
            } else if (!cap.isFlying()) {
                cap.setFlying(true);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setRotationAngles(PlayerEntity player, HUSetRotationAnglesEvent event) {
        IRex cap = RexCap.getCap(player);
        if (cap.isFlying()) {
            if (!player.isOnGround() && !player.isSwimming() && player.isSprinting()) {
                event.getPlayerModel().bipedRightArm.rotateAngleX = (float) Math.toRadians(180F);
            }
        }
        if (cap.getType() == 2) {
            float f = player.ticksExisted + event.getAgeInTicks();
            float rotationX = (float) Math.toRadians(-(45F + (MathHelper.cos(f))));
            if (player.getPrimaryHand() == HandSide.RIGHT) {
                event.getPlayerModel().bipedRightArm.rotateAngleX = rotationX;
                event.getPlayerModel().bipedRightArm.rotateAngleZ = (float) Math.toRadians(-45F);
            }else{
                event.getPlayerModel().bipedLeftArm.rotateAngleX = rotationX;
                event.getPlayerModel().bipedLeftArm.rotateAngleZ = (float) Math.toRadians(45F);
            }
            GRClientUtil.copyAnglesToWear(event.getPlayerModel());
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        IRex cap = RexCap.getCap(player);
        if (cap.getType() == 1) {
            TBClientUtil.renderHeatvision(renderer, matrix, bufferIn, packedLightIn, player, true, 1f, 0f, 0f);
            TBClientUtil.renderHeatvision(renderer, matrix, bufferIn, packedLightIn, player, false, 1f, 0f, 0f);
        }
    }

    @Mod.EventBusSubscriber(modid = TheBoys.MODID)
    public static class HomelanderEvents {

        @SubscribeEvent
        public static void onBurnDamage(LivingAttackEvent event) {
            if (event.getEntityLiving() instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) event.getEntityLiving();
                if (AbilityHelper.getEnabled(TBAbilityTypes.HOMELANDER, player)) {
                    if (event.getSource().equals(DamageSource.LAVA)
                            || event.getSource().equals(DamageSource.IN_FIRE)
                            || event.getSource().equals(DamageSource.ON_FIRE)) {
                        event.setCanceled(true);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void livingFall(LivingFallEvent e) {
            if (e.getEntityLiving() instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) e.getEntityLiving();

                if (AbilityHelper.getEnabled(TBAbilityTypes.HOMELANDER, player) && e.getDistance() > 20) {
                    for (int x = 0; x < 5; x++) {
                        for (int y = 0; y < 5; y++) {
                            for (int z = 0; z < 5; z++) {
                                double xPos = player.getPosX() - 2.5 + x + player.world.rand.nextInt(5);
                                double yPos = player.getPosY() - 2.5 + y + player.world.rand.nextInt(5);
                                double zPos = player.getPosZ() - 2.5 + z + player.world.rand.nextInt(5);
                                BlockPos pos = new BlockPos(xPos, yPos, zPos);
                                Block block = player.world.getBlockState(pos).getBlock();

                                if (block == Blocks.STONE) {
                                    player.world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
                                } else if (block == Blocks.STONE_BRICKS) {
                                    player.world.setBlockState(pos, Blocks.CRACKED_STONE_BRICKS.getDefaultState());
                                } else if (block == Blocks.COBBLESTONE) {
                                    player.world.setBlockState(pos, Blocks.GRAVEL.getDefaultState());
                                } else if (block == Blocks.GRASS_BLOCK || block == Blocks.DIRT) {
                                    player.world.setBlockState(pos, Blocks.COARSE_DIRT.getDefaultState());
                                } else if (block == Blocks.OAK_LOG) {
                                    player.world.setBlockState(pos, Blocks.STRIPPED_OAK_LOG.getDefaultState());
                                } else if (block == Blocks.BIRCH_LOG) {
                                    player.world.setBlockState(pos, Blocks.STRIPPED_BIRCH_LOG.getDefaultState());
                                } else if (block == Blocks.SPRUCE_LOG) {
                                    player.world.setBlockState(pos, Blocks.STRIPPED_SPRUCE_LOG.getDefaultState());
                                } else if (block == Blocks.JUNGLE_LOG) {
                                    player.world.setBlockState(pos, Blocks.STRIPPED_JUNGLE_LOG.getDefaultState());
                                } else if (block == Blocks.DARK_OAK_LOG) {
                                    player.world.setBlockState(pos, Blocks.STRIPPED_DARK_OAK_LOG.getDefaultState());
                                } else if (block == Blocks.ACACIA_LOG) {
                                    player.world.setBlockState(pos, Blocks.STRIPPED_ACACIA_LOG.getDefaultState());
                                } else if (block == Blocks.GLASS) {
                                    player.world.destroyBlock(pos, false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
