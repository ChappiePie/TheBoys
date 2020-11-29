package chappie.theboys.abilities;

import chappie.theboys.TheBoys;
import chappie.theboys.util.TBClientUtil;
import chappie.theboys.util.TBUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import xyz.heroesunited.heroesunited.client.HUClientEventHandler;
import xyz.heroesunited.heroesunited.client.events.HUSetRotationAnglesEvent;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;
import xyz.heroesunited.heroesunited.common.abilities.IFlyingAbility;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayer;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayerProvider;
import xyz.heroesunited.heroesunited.common.capabilities.IHUPlayer;
import xyz.heroesunited.heroesunited.common.networking.HUData;
import xyz.heroesunited.heroesunited.common.networking.HUNetworking;
import xyz.heroesunited.heroesunited.common.networking.server.ServerSetHUData;
import xyz.heroesunited.heroesunited.common.objects.HUAttributes;
import xyz.heroesunited.heroesunited.hupacks.HUPackSuperpowers;
import xyz.heroesunited.heroesunited.util.HUClientUtil;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;

import java.awt.*;
import java.util.UUID;

public class HomelanderAbility extends Ability implements IFlyingAbility {

    @Override
    public void onUpdate(PlayerEntity player) {
        IHUPlayer cap = HUPlayer.getCap(player);
        if (cap.isFlying() && player.getPosY() > 500.0D) {
            cap.setFlying(false);
        }
        if (player.world.isRemote && !HUClientEventHandler.ABILITY_KEYS.get(1).isPressed() && cap.getType() == 1) {
            HUNetworking.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ServerSetHUData(HUData.TYPE, 0));
        }
        if (cap.getType() == 1) {
            HUPlayerUtil.makeLaserLooking(player);
        }
    }

    @Override
    public void toggle(PlayerEntity player, int id, int action) {
        IHUPlayer cap = HUPlayer.getCap(player);
        if (id == 1) {
            cap.setType(1);
        } else if (id == 2 && action < GLFW.GLFW_REPEAT) {
            cap.setFlying(!cap.isFlying());
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setRotationAngles(HUSetRotationAnglesEvent event) {
        IHUPlayer cap = HUPlayer.getCap(event.getPlayer());
        if (cap.getType() == 2) {
            float f = event.getPlayer().ticksExisted + event.getAgeInTicks();
            float rotationX = (float) Math.toRadians(-(45F + (MathHelper.cos(f))));
            if (event.getPlayer().getPrimaryHand() == HandSide.RIGHT) {
                event.getPlayerModel().bipedRightArm.rotateAngleX = rotationX;
                event.getPlayerModel().bipedRightArm.rotateAngleZ = (float) Math.toRadians(-45F);
            } else {
                event.getPlayerModel().bipedLeftArm.rotateAngleX = rotationX;
                event.getPlayerModel().bipedLeftArm.rotateAngleZ = (float) Math.toRadians(45F);
            }
            HUClientUtil.copyAnglesToWear(event.getPlayerModel());
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        IHUPlayer cap = HUPlayer.getCap(player);
        if (cap.getType() == 1) {
            TBClientUtil.renderHeatvision(renderer, matrix, bufferIn, packedLightIn, player, true, Color.RED);
            TBClientUtil.renderHeatvision(renderer, matrix, bufferIn, packedLightIn, player, false, Color.RED);
        }
    }

    @Override
    public void onActivated(PlayerEntity player) {
        super.onActivated(player);
        setDefaultAttributes(player);
    }

    @Override
    public void onDeactivated(PlayerEntity player) {
        super.onDeactivated(player);
        HUPlayer.getCap(player).setType(0);
        HUPlayer.getCap(player).setFlying(false);
        setDefaultAttributes(player);
    }

    private void setDefaultAttributes(PlayerEntity player) {
        final UUID ATTRIBUTE_UUID = UUID.fromString("831d9761-3e36-46a6-ba4b-7a2e38d80d3b");
        AbilityHelper.setAttribute(player, "Homelander", Attributes.MAX_HEALTH, ATTRIBUTE_UUID, 10D, AttributeModifier.Operation.ADDITION);
        AbilityHelper.setAttribute(player, "Homelander", HUAttributes.JUMP_BOOST, ATTRIBUTE_UUID, 2D, AttributeModifier.Operation.ADDITION);
        AbilityHelper.setAttribute(player, "Homelander", Attributes.ATTACK_DAMAGE, ATTRIBUTE_UUID, 2.0D, AttributeModifier.Operation.ADDITION);
        AbilityHelper.setAttribute(player, "Homelander", HUAttributes.FALL_RESISTANCE, ATTRIBUTE_UUID, -1.0D, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public boolean renderFlying(PlayerEntity player) {
        return true;
    }

    @Override
    public boolean rotateArms() {
        return true;
    }

    @Override
    public SoundEvent getSoundEvent() {
        return null;
    }

    @Mod.EventBusSubscriber(modid = TheBoys.MODID)
    public static class Events {

        @SubscribeEvent
        public static void onBurnDamage(LivingAttackEvent event) {
            if (event.getEntityLiving() instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) event.getEntityLiving();
                player.getCapability(HUPlayerProvider.CAPABILITY).ifPresent(cap -> {
                    if (cap.getSuperpower() == HUJsonUtils.getSuperpower(TheBoys.MODID, "homelander")) {
                        if (event.getSource().equals(DamageSource.LAVA) || event.getSource().equals(DamageSource.IN_FIRE) || event.getSource().equals(DamageSource.ON_FIRE)) {
                            event.setCanceled(true);
                        }
                    }
                });
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
                                } else if (block == Blocks.GRASS_BLOCK) {
                                    player.world.setBlockState(pos, Blocks.DIRT.getDefaultState());
                                } else if (block == Blocks.DIRT) {
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
