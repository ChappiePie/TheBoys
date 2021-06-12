package chappie.theboys.common;

import chappie.theboys.abilities.IAbilityTick;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;
import xyz.heroesunited.heroesunited.common.abilities.FlightAbility;

public class TBEventHandler {

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            AbilityHelper.getAbilities(e.player).forEach(type -> {
                if (type instanceof IAbilityTick) {
                    ((IAbilityTick) type).tick(e.player, e.side);
                }
            });
        }
    }

    @SubscribeEvent
    public void livingFall(LivingFallEvent e) {
        if (e.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) e.getEntityLiving();
            for (Ability ability : AbilityHelper.getAbilities(player)) {
                if (ability instanceof FlightAbility && JSONUtils.getAsBoolean(ability.getJsonObject(), "break_blocks", false) && e.getDistance() > 20) {
                    for (int x = 0; x < 5; x++) {
                        for (int y = 0; y < 5; y++) {
                            for (int z = 0; z < 5; z++) {
                                double xPos = player.getX() - 2.5 + x + player.level.random.nextInt(5);
                                double yPos = player.getY() - 2.5 + y + player.level.random.nextInt(5);
                                double zPos = player.getZ() - 2.5 + z + player.level.random.nextInt(5);
                                BlockPos pos = new BlockPos(xPos, yPos, zPos);
                                Block block = player.level.getBlockState(pos).getBlock();

                                if (block == Blocks.STONE) {
                                    player.level.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());
                                } else if (block == Blocks.STONE_BRICKS) {
                                    player.level.setBlockAndUpdate(pos, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
                                } else if (block == Blocks.COBBLESTONE) {
                                    player.level.setBlockAndUpdate(pos, Blocks.GRAVEL.defaultBlockState());
                                } else if (block == Blocks.GRASS_BLOCK) {
                                    player.level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
                                } else if (block == Blocks.DIRT) {
                                    player.level.setBlockAndUpdate(pos, Blocks.COARSE_DIRT.defaultBlockState());
                                } else if (block == Blocks.OAK_LOG) {
                                    player.level.setBlockAndUpdate(pos, Blocks.STRIPPED_OAK_LOG.defaultBlockState());
                                } else if (block == Blocks.BIRCH_LOG) {
                                    player.level.setBlockAndUpdate(pos, Blocks.STRIPPED_BIRCH_LOG.defaultBlockState());
                                } else if (block == Blocks.SPRUCE_LOG) {
                                    player.level.setBlockAndUpdate(pos, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
                                } else if (block == Blocks.JUNGLE_LOG) {
                                    player.level.setBlockAndUpdate(pos, Blocks.STRIPPED_JUNGLE_LOG.defaultBlockState());
                                } else if (block == Blocks.DARK_OAK_LOG) {
                                    player.level.setBlockAndUpdate(pos, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
                                } else if (block == Blocks.ACACIA_LOG) {
                                    player.level.setBlockAndUpdate(pos, Blocks.STRIPPED_ACACIA_LOG.defaultBlockState());
                                } else if (block == Blocks.GLASS) {
                                    player.level.destroyBlock(pos, false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}