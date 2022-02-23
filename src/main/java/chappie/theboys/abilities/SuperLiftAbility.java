package chappie.theboys.abilities;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import xyz.heroesunited.heroesunited.client.events.SetupAnimEvent;
import xyz.heroesunited.heroesunited.common.abilities.AbilityType;
import xyz.heroesunited.heroesunited.common.abilities.IAbilityClientProperties;
import xyz.heroesunited.heroesunited.common.abilities.JSONAbility;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class SuperLiftAbility extends JSONAbility {

    public SuperLiftAbility(AbilityType type, Player player, JsonObject jsonObject) {
        super(type, player, jsonObject);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register("cooldownLift", 0);
    }

    @Override
    public void action(Player player) {
        super.action(player);
        if (this.dataManager.<Integer>getValue("cooldownLift") > 0) {
            this.dataManager.set("cooldownLift", this.dataManager.<Integer>getValue("cooldownLift") - 1);
        }

        if (this.getEnabled() && this.dataManager.<Integer>getValue("cooldownLift") == 0 && player.level instanceof ServerLevel) {
            Vec3 posVc3d = new Vec3(player.getX(), player.getY() + player.getBbHeight(), player.getZ());
            BlockPos pos = new BlockPos(posVc3d.x + 1, posVc3d.y, posVc3d.z + 1);
            BlockPos pos1 = new BlockPos(posVc3d.x - 1, posVc3d.y + 1, posVc3d.z - 1);
            clone((ServerLevel) player.level, BoundingBox.fromCorners(pos, pos1));
        }
    }

    /**
     * Made by Mojang,
     * @see net.minecraft.server.commands.CloneCommands
     * @method clone
     *
     * Edited by Chappie
     */
    private void clone(ServerLevel world, BoundingBox box) {
        List<BlockInfo> list = Lists.newArrayList(),list1 = Lists.newArrayList(),list2 = Lists.newArrayList();
        LinkedList<BlockPos> deque = Lists.newLinkedList();
        BlockPos pos2 = new BlockPos(0, 1, 0);

        for (int j = box.minZ(); j <= box.maxZ(); ++j) {
            for (int k = box.minY(); k <= box.maxY(); ++k) {
                for (int l = box.minX(); l <= box.maxX(); ++l) {
                    BlockPos fuck = new BlockPos(box.getCenter().getX(), k, box.getCenter().getZ());
                    BlockPos blockPos = new BlockPos(l, k, j);
                    BlockPos blockPos1 = blockPos.offset(pos2);
                    if (world.getBlockState(fuck).getMaterial() != Material.AIR && blockPos1.getY() < world.getMaxBuildHeight()) {
                        BlockState blockstate = world.getBlockState(blockPos);
                        BlockState blockstate1 = world.getBlockState(blockPos1);
                        if (blockstate.getMaterial() != Material.AIR && blockstate1.getMaterial() == Material.AIR) {
                            BlockEntity tileentity = world.getBlockEntity(blockPos);
                            if (tileentity != null) {
                                CompoundTag compoundnbt = tileentity.saveWithoutMetadata();
                                list1.add(new BlockInfo(blockPos1, blockstate, compoundnbt));
                                deque.addLast(blockPos);
                            } else if (!blockstate.isSolidRender(world, blockPos) && !blockstate.isCollisionShapeFullBlock(world, blockPos)) {
                                list2.add(new BlockInfo(blockPos1, blockstate, null));
                                deque.addFirst(blockPos);
                            } else {
                                list.add(new BlockInfo(blockPos1, blockstate, null));
                                deque.addLast(blockPos);
                            }
                        }
                    }
                }
            }
        }

        for(BlockPos blockpos4 : deque) {
            Clearable.tryClear(world.getBlockEntity(blockpos4));
        }

        for(BlockPos blockpos5 : deque) {
            world.setBlock(blockpos5, Blocks.AIR.defaultBlockState(), 3);
        }

        List<BlockInfo> list3 = Lists.newArrayList();
        list3.addAll(list);
        list3.addAll(list1);
        list3.addAll(list2);
        List<BlockInfo> list4 = Lists.reverse(list3);

        for (BlockInfo info : list4) {
            Clearable.tryClear(world.getBlockEntity(info.pos));
        }

        for (BlockInfo info : list3) {
            world.setBlock(info.pos, info.state, 2);
        }

        for (BlockInfo info : list1) {
            BlockEntity tile = world.getBlockEntity(info.pos);
            if (info.tag != null && tile != null) {
                info.tag.putInt("x", info.pos.getX());
                info.tag.putInt("y", info.pos.getY());
                info.tag.putInt("z", info.pos.getZ());
                tile.load(info.tag);
                tile.setChanged();
            }

            world.setBlock(info.pos, info.state, 2);
        }

        for (BlockInfo info : list4) {
            world.blockUpdated(info.pos, info.state.getBlock());
        }

        world.getBlockTicks().copyArea(box, pos2);
        this.dataManager.set("cooldownLift", 20);
    }

    @Override
    public void initializeClient(Consumer<IAbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IAbilityClientProperties() {
            @Override
            public void setupAnim(SetupAnimEvent event) {
                if (getEnabled()) {
                    event.getPlayerModel().rightArm.xRot = event.getPlayerModel().leftArm.xRot = (float) Math.toRadians(180F);
                }
            }
        });
    }

    public static class BlockInfo {
        public final BlockPos pos;
        public final BlockState state;
        public final CompoundTag tag;

        public BlockInfo(BlockPos pos, BlockState state, CompoundTag nbt) {
            this.pos = pos;
            this.state = state;
            this.tag = nbt;
        }
    }
}