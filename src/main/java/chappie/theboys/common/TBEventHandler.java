package chappie.theboys.common;

import chappie.theboys.common.capability.BoysCap;
import chappie.theboys.util.TBUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;
import xyz.heroesunited.heroesunited.common.abilities.FlightAbility;
import xyz.heroesunited.heroesunited.common.events.HURegisterDataEvent;

public class TBEventHandler {

    @SubscribeEvent
    public void playerTick(HURegisterDataEvent event) {
        event.register("ShootsFromEyes", false);
    }

    @SubscribeEvent
    public void livingFall(LivingFallEvent e) {
        if (e.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) e.getEntityLiving();
            for (Ability ability : AbilityHelper.getAbilities(player)) {
                if (ability instanceof FlightAbility && JSONUtils.getBoolean(ability.getJsonObject(), "break_blocks", false) && e.getDistance() > 20) {
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